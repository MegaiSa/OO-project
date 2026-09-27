package fr.efrei.messaging.server.service;

import fr.efrei.messaging.server.entity.Message;
import fr.efrei.messaging.server.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business layer: rules about messages. Knows nothing about gRPC.
 */
@Service
public class MessageService {

    public static final int MAX_LENGTH = 2000;

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository repository;

    @Autowired
    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Message send(long conversationId, long senderId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content must not be empty");
        }
        if (content.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Message content must not exceed " + MAX_LENGTH + " characters");
        }
        Message saved = repository.save(new Message(conversationId, senderId, content.strip()));
        log.info("Message {} stored (conversation={}, sender={})", saved.getId(), conversationId, senderId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Message> history(long conversationId) {
        List<Message> messages = repository.findByConversationIdOrderBySentAtAscIdAsc(conversationId);
        log.debug("History of conversation {}: {} message(s)", conversationId, messages.size());
        return messages;
    }

    @Transactional(readOnly = true)
    public long count(long conversationId) {
        return repository.countByConversationId(conversationId);
    }
}
