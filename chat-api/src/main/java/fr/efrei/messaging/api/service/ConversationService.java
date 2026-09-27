package fr.efrei.messaging.api.service;

import fr.efrei.messaging.api.entity.Conversation;
import fr.efrei.messaging.api.entity.User;
import fr.efrei.messaging.api.exception.BadRequestException;
import fr.efrei.messaging.api.exception.ResourceNotFoundException;
import fr.efrei.messaging.api.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepository;
    private final UserService userService;

    @Autowired
    public ConversationService(ConversationRepository conversationRepository, UserService userService) {
        this.conversationRepository = conversationRepository;
        this.userService = userService;
    }

    @Transactional
    public Conversation create(String name, List<Long> participantIds) {
        Set<Long> distinctIds = new LinkedHashSet<>(participantIds);
        if (distinctIds.size() < 2) {
            throw new BadRequestException("A conversation needs at least 2 distinct participants");
        }
        Set<User> participants = new LinkedHashSet<>();
        for (Long id : distinctIds) {
            participants.add(userService.findById(id)); // 404 if a participant does not exist
        }
        Conversation conversation = conversationRepository.save(new Conversation(name.strip(), participants));
        log.info("Conversation '{}' created (id={}, participants={})", conversation.getName(), conversation.getId(), distinctIds);
        return conversation;
    }

    @Transactional(readOnly = true)
    public Conversation findById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", id));
    }

    @Transactional(readOnly = true)
    public List<Conversation> findByUser(Long userId) {
        userService.findById(userId); // 404 if the user does not exist
        return conversationRepository.findByParticipantsId(userId);
    }
}
