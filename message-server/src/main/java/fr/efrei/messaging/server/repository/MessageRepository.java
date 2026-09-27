package fr.efrei.messaging.server.repository;

import fr.efrei.messaging.server.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderBySentAtAscIdAsc(Long conversationId);

    long countByConversationId(Long conversationId);
}
