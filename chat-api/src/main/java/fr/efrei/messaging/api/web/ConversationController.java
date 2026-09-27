package fr.efrei.messaging.api.web;

import fr.efrei.messaging.api.service.ConversationService;
import fr.efrei.messaging.api.service.MessagingService;
import fr.efrei.messaging.api.web.dto.ConversationResponse;
import fr.efrei.messaging.api.web.dto.CreateConversationRequest;
import fr.efrei.messaging.api.web.dto.MessageResponse;
import fr.efrei.messaging.api.web.dto.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessagingService messagingService;

    @Autowired
    public ConversationController(ConversationService conversationService, MessagingService messagingService) {
        this.conversationService = conversationService;
        this.messagingService = messagingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse create(@Valid @RequestBody CreateConversationRequest request) {
        return messagingService.describe(conversationService.create(request.name(), request.participantIds()));
    }

    @GetMapping("/{id}")
    public ConversationResponse get(@PathVariable Long id) {
        return messagingService.describe(conversationService.findById(id));
    }

    /** Stored in message-server through a gRPC unary call. */
    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@PathVariable Long id, @Valid @RequestBody SendMessageRequest request) {
        return messagingService.send(id, request.senderId(), request.content());
    }

    /** Read from message-server through a gRPC server-streaming call. */
    @GetMapping("/{id}/messages")
    public List<MessageResponse> history(@PathVariable Long id) {
        return messagingService.history(id);
    }
}
