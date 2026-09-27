package fr.efrei.messaging.api.web;

import fr.efrei.messaging.api.service.ConversationService;
import fr.efrei.messaging.api.service.MessagingService;
import fr.efrei.messaging.api.service.UserService;
import fr.efrei.messaging.api.web.dto.ConversationResponse;
import fr.efrei.messaging.api.web.dto.CreateUserRequest;
import fr.efrei.messaging.api.web.dto.UpdateUserRequest;
import fr.efrei.messaging.api.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Web layer: HTTP <-> Java. No business rule here, only delegation to the services.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ConversationService conversationService;
    private final MessagingService messagingService;

    @Autowired
    public UserController(UserService userService, ConversationService conversationService,
                          MessagingService messagingService) {
        this.userService = userService;
        this.conversationService = conversationService;
        this.messagingService = messagingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.create(request.username(), request.displayName()));
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.findAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(userService.findById(id));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(userService.updateDisplayName(id, request.displayName()));
    }

    @GetMapping("/{id}/conversations")
    public List<ConversationResponse> conversations(@PathVariable Long id) {
        return messagingService.describeAll(conversationService.findByUser(id));
    }
}
