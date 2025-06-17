package hse.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hse.event.PaymentProcessedEvent;
import hse.exception.OrdersException;
import hse.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessedListener {
    private final OrdersService ordersService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment.processed",
            groupId = "orders-service-group")
    @Transactional
    public void listenPaymentStatus(ConsumerRecord<String, String> consumerRecord, Acknowledgment ack) {
        String topic = consumerRecord.topic();
        String payload = consumerRecord.value();
        String messageId = consumerRecord.headers().lastHeader("id") != null ?
                new String(consumerRecord.headers().lastHeader("id").value()) :
                consumerRecord.key();

        log.info("Received payment status message from Kafka topic '{}' with key '{}', offset '{}', messageId: {}",
                topic, consumerRecord.key(), consumerRecord.offset(), messageId);

        try {
            if (payload == null || payload.trim().isEmpty()) {
                log.warn("Received an empty or null payload for message ID {}. Skipping processing.", messageId);
                ack.acknowledge();
                return;
            }

            JsonNode rootNode = objectMapper.readTree(payload);

            JsonNode paymentStatusNode = rootNode.get("paymentStatus");
            if (paymentStatusNode == null || !paymentStatusNode.isTextual()) {
                log.error("Payload for message ID {} does not contain a valid 'paymentStatus' field. Payload: {}", messageId, payload);

                throw new OrdersException("Invalid payload: missing or malformed 'paymentStatus' field.");
            }

            String paymentStatus = paymentStatusNode.asText();
            PaymentProcessedEvent event = objectMapper.readValue(payload, PaymentProcessedEvent.class);

            if ("SUCCESS".equals(paymentStatus)) {
                log.info("Processing PaymentSuccessEvent for orderId: {}", event.orderId());
                ordersService.updateOrderStatus(event, true);
            } else if ("FAILURE".equals(paymentStatus)) {
                log.info("Processing PaymentFailedEvent for orderId: {}. Reason: {}", event.orderId(), event.reason());
                ordersService.updateOrderStatus(event, false);
            } else {
                log.warn("Unknown paymentStatus '{}' for message ID {}. Skipping processing. Payload: {}", paymentStatus, messageId, payload);
                ack.acknowledge();
                return;
            }

            ack.acknowledge();
            log.info("Successfully processed and acknowledged payment status message with ID {}.", messageId);

        } catch (Exception e) {
            log.error("Error processing payment status message for key {}: {}. Transaction will rollback.",
                    consumerRecord.key(), e.getMessage(), e);

            throw new OrdersException("Failed to process payment status event for message ID " + messageId, e);
        }
    }
}