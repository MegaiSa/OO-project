package fr.efrei.messaging.server.grpc;

import fr.efrei.messaging.grpc.CountReply;
import fr.efrei.messaging.grpc.HistoryRequest;
import fr.efrei.messaging.grpc.MessageReply;
import fr.efrei.messaging.grpc.MessageServiceGrpc;
import fr.efrei.messaging.grpc.SendMessageRequest;
import fr.efrei.messaging.server.entity.Message;
import fr.efrei.messaging.server.service.MessageService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Web (gRPC) layer: translates gRPC requests into calls to the business layer.
 * Stateless: nothing is kept between two calls, everything is in the database.
 */
@GrpcService
public class MessageGrpcService extends MessageServiceGrpc.MessageServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(MessageGrpcService.class);

    private final MessageService messageService;

    @Autowired
    public MessageGrpcService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<MessageReply> responseObserver) {
        log.debug("gRPC SendMessage received for conversation {}", request.getConversationId());
        try {
            Message message = messageService.send(request.getConversationId(), request.getSenderId(), request.getContent());
            responseObserver.onNext(toReply(message));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid message rejected: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getHistory(HistoryRequest request, StreamObserver<MessageReply> responseObserver) {
        log.debug("gRPC GetHistory received for conversation {}", request.getConversationId());
        // Server streaming: each message is pushed one by one on the same connection
        for (Message message : messageService.history(request.getConversationId())) {
            responseObserver.onNext(toReply(message));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void countMessages(HistoryRequest request, StreamObserver<CountReply> responseObserver) {
        long count = messageService.count(request.getConversationId());
        responseObserver.onNext(CountReply.newBuilder().setCount(count).build());
        responseObserver.onCompleted();
    }

    private static MessageReply toReply(Message message) {
        return MessageReply.newBuilder()
                .setId(message.getId())
                .setConversationId(message.getConversationId())
                .setSenderId(message.getSenderId())
                .setContent(message.getContent())
                .setSentAt(message.getSentAt().toEpochMilli())
                .build();
    }
}
