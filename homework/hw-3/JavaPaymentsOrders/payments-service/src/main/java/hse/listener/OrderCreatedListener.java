package hse.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hse.event.OrderCreatedEvent;
import hse.exception.PaymentsException;
import hse.model.InboxEntity;
import hse.repository.InboxRepository;
import hse.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {
    private final InboxRepository inboxRepository;
    private final PaymentsService paymentsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order.created",
            groupId = "payments-service-group")
    @Transactional
    public void listenOrderCreated(ConsumerRecord<String, String> consumerRecord, Acknowledgment ack) {
        String messageId;

        if (consumerRecord.key() != null && !consumerRecord.key().isEmpty()) {
            messageId = consumerRecord.key();
        }

        else if (consumerRecord.headers().lastHeader("id") != null) {
            messageId = new String(consumerRecord.headers().lastHeader("id").value());
            log.warn("Kafka record key is null or empty. Using 'id' header for messageId: {}", messageId);
        }

        else {
            messageId = UUID.randomUUID().toString();
            log.warn("Neither Kafka record key nor 'id' header available. Generated random UUID for messageId: {}", messageId);
        }

        String topic = consumerRecord.topic();
        String payload = consumerRecord.value();

        log.info("Received message from Kafka topic '{}' with key '{}', offset '{}', messageId: {}",
                topic, consumerRecord.key(), consumerRecord.offset(), messageId);

        if (inboxRepository.findByMessageId(messageId).isPresent()) {
            log.warn("Message with ID {} already processed. Skipping.", messageId);
            ack.acknowledge();
            return;
        }

        try {
            if (payload == null || payload.trim().isEmpty()) {
                log.error("Received an empty or null payload for message ID {}. Skipping processing.", messageId);
                ack.acknowledge();
                return;
            }

            InboxEntity inbox = InboxEntity.builder()
                    .messageId(messageId)
                    .topic(topic)
                    .payload(payload)
                    .processed(false)
                    .build();
            inboxRepository.save(inbox);

            OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(payload, OrderCreatedEvent.class);
            paymentsService.processOrderPayment(orderCreatedEvent);

            inbox.setProcessed(true);
            inboxRepository.save(inbox);

            ack.acknowledge();
            log.info("Successfully processed message with ID {} and acknowledged.", messageId);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing message payload: {}. Message ID: {}", payload, messageId, e);
            throw new PaymentsException("Error processing Kafka message due to JSON deserialization failure", e);
        } catch (Exception e) {
            log.error("Error processing OrderCreated event for message ID {}: {}", messageId, e.getMessage(), e);
            throw new PaymentsException("Error processing Kafka message", e);
        }
    }
}