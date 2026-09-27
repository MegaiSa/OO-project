package fr.efrei.messaging.api.repository;

import fr.efrei.messaging.api.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByParticipantsId(Long userId);
}
