package fr.efrei.messaging.api;

import fr.efrei.messaging.api.entity.Conversation;
import fr.efrei.messaging.api.entity.User;
import fr.efrei.messaging.api.exception.ForbiddenException;
import fr.efrei.messaging.api.grpc.MessageClient;
import fr.efrei.messaging.api.service.ConversationService;
import fr.efrei.messaging.api.service.MessagingService;
import fr.efrei.messaging.api.service.UserService;
import fr.efrei.messaging.api.web.dto.ConversationResponse;
import fr.efrei.messaging.api.web.dto.MessageResponse;
import fr.efrei.messaging.grpc.MessageReply;
import io.grpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessagingServiceTest {

    private final ConversationService conversationService = mock(ConversationService.class);
    private final UserService userService = mock(UserService.class);
    private final MessageClient messageClient = mock(MessageClient.class);
    private final MessagingService messagingService =
            new MessagingService(conversationService, userService, messageClient);

    private User alice;
    private User bob;
    private User eve;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        alice = user(1L, "alice");
        bob = user(2L, "bob");
        eve = user(3L, "eve");
        conversation = new Conversation("Projet", Set.of(alice, bob));
        ReflectionTestUtils.setField(conversation, "id", 10L);
        when(conversationService.findById(10L)).thenReturn(conversation);
    }

    @Test
    void participantCanSendMessage() {
        when(userService.findById(1L)).thenReturn(alice);
        when(messageClient.send(10L, 1L, "Salut")).thenReturn(MessageReply.newBuilder()
                .setId(100L).setConversationId(10L).setSenderId(1L).setContent("Salut").setSentAt(0L).build());

        MessageResponse response = messagingService.send(10L, 1L, "Salut");

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.senderUsername()).isEqualTo("alice");
    }

    @Test
    void nonParticipantIsRejectedWithoutCallingGrpc() {
        when(userService.findById(3L)).thenReturn(eve);

        assertThatThrownBy(() -> messagingService.send(10L, 3L, "Coucou"))
                .isInstanceOf(ForbiddenException.class);
        verify(messageClient, never()).send(anyLong(), anyLong(), anyString());
    }

    @Test
    void conversationIsStillReturnedWhenMessageServerIsDown() {
        when(messageClient.count(10L)).thenThrow(Status.UNAVAILABLE.asRuntimeException());

        ConversationResponse response = messagingService.describe(conversation);

        assertThat(response.name()).isEqualTo("Projet");
        assertThat(response.messageCount()).isNull();
    }

    private static User user(Long id, String username) {
        User user = new User(username, username);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
