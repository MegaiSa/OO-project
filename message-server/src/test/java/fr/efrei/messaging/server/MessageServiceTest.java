package fr.efrei.messaging.server;

import fr.efrei.messaging.server.entity.Message;
import fr.efrei.messaging.server.repository.MessageRepository;
import fr.efrei.messaging.server.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class MessageServiceTest {

    @Autowired
    private MessageRepository repository;

    @Test
    void storesAndReturnsHistoryInOrder() {
        MessageService service = new MessageService(repository);
        service.send(1L, 10L, "Salut");
        service.send(1L, 11L, "Hello !");
        service.send(2L, 10L, "Autre conversation");

        List<Message> history = service.history(1L);

        assertThat(history).extracting(Message::getContent).containsExactly("Salut", "Hello !");
        assertThat(service.count(1L)).isEqualTo(2);
    }

    @Test
    void rejectsBlankMessage() {
        MessageService service = new MessageService(repository);

        assertThatThrownBy(() -> service.send(1L, 10L, "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
