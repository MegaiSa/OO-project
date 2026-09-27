package fr.efrei.messaging.api.service;

import fr.efrei.messaging.api.entity.Conversation;
import fr.efrei.messaging.api.entity.User;
import fr.efrei.messaging.api.exception.ForbiddenException;
import fr.efrei.messaging.api.grpc.MessageClient;
import fr.efrei.messaging.api.web.dto.ConversationResponse;
import fr.efrei.messaging.api.web.dto.MessageResponse;
import fr.efrei.messaging.api.web.dto.UserResponse;
import fr.efrei.messaging.grpc.MessageReply;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrates the two services: checks users/conversations in the local database,
 * then delegates message storage to message-server through gRPC.
 */
@Service
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger(MessagingService.class);

    private final ConversationService conversationService;
    private final UserService userService;
    private final MessageClient messageClient;

    @Autowired
    public MessagingService(ConversationService conversationService, UserService userService, MessageClient messageClient) {
        this.conversationService = conversationService;
        this.userService = userService;
        this.messageClient = messageClient;
    }

    public MessageResponse send(Long conversationId, Long senderId, String content) {
        Conversation conversation = conversationService.findById(conversationId); // 404
        User sender = userService.findById(senderId);                             // 404
        if (!conversation.hasParticipant(senderId)) {
            throw new ForbiddenException("User " + senderId + " is not a participant of conversation " + conversationId);
        }
        MessageReply reply = messageClient.send(conversationId, senderId, content);
        log.info("Message {} sent by {} in conversation {}", reply.getId(), sender.getUsername(), conversationId);
        return toResponse(reply, sender.getUsername());
    }

    public List<MessageResponse> history(Long conversationId) {
        Conversation conversation = conversationService.findById(conversationId); // 404
        Map<Long, String> usernames = conversation.getParticipants().stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        return messageClient.history(conversationId).stream()
                .map(m -> toResponse(m, usernames.getOrDefault(m.getSenderId(), "unknown")))
                .toList();
    }

    /**
     * Builds the conversation view. If message-server is down, the conversation is still
     * returned (messageCount = null) instead of failing: "design for failure".
     */
    public ConversationResponse describe(Conversation conversation) {
        Long count;
        try {
            count = messageClient.count(conversation.getId());
        } catch (StatusRuntimeException e) {
            log.warn("Could not count messages of conversation {}: {}", conversation.getId(), e.getStatus().getCode());
            count = null;
        }
        List<UserResponse> participants = conversation.getParticipants().stream()
                .map(UserResponse::from)
                .toList();
        return new ConversationResponse(conversation.getId(), conversation.getName(), participants, count,
                conversation.getCreatedAt());
    }

    public List<ConversationResponse> describeAll(List<Conversation> conversations) {
        return conversations.stream().map(this::describe).toList();
    }

    private static MessageResponse toResponse(MessageReply reply, String senderUsername) {
        return new MessageResponse(reply.getId(), reply.getConversationId(), reply.getSenderId(), senderUsername,
                reply.getContent(), Instant.ofEpochMilli(reply.getSentAt()));
    }
}
