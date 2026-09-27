package fr.efrei.messaging.api.grpc;

import fr.efrei.messaging.grpc.HistoryRequest;
import fr.efrei.messaging.grpc.MessageReply;
import fr.efrei.messaging.grpc.MessageServiceGrpc;
import fr.efrei.messaging.grpc.SendMessageRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * gRPC client of message-server. Hides the generated stub from the rest of the application.
 * The stub is created and injected by Spring (grpc-client-spring-boot-starter).
 */
@Component
public class MessageClient {

    private static final Logger log = LoggerFactory.getLogger(MessageClient.class);

    @GrpcClient("message-server")
    private MessageServiceGrpc.MessageServiceBlockingStub stub;

    public MessageReply send(long conversationId, long senderId, String content) {
        log.debug("gRPC -> SendMessage(conversation={}, sender={})", conversationId, senderId);
        SendMessageRequest request = SendMessageRequest.newBuilder()
                .setConversationId(conversationId)
                .setSenderId(senderId)
                .setContent(content)
                .build();
        return stub.sendMessage(request);
    }

    public List<MessageReply> history(long conversationId) {
        log.debug("gRPC -> GetHistory(conversation={})", conversationId);
        // Server streaming: the blocking stub exposes the stream as an Iterator
        Iterator<MessageReply> stream = stub.getHistory(request(conversationId));
        List<MessageReply> messages = new ArrayList<>();
        stream.forEachRemaining(messages::add);
        return messages;
    }

    public long count(long conversationId) {
        return stub.countMessages(request(conversationId)).getCount();
    }

    private static HistoryRequest request(long conversationId) {
        return HistoryRequest.newBuilder().setConversationId(conversationId).build();
    }
}
