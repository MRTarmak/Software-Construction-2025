package hse.scheduler;

import hse.model.OutboxEntity;
import hse.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class OutboxScheduler {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 5000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOutboxEvents() {
        log.info("Attempting to process Payments Service outbox events...");
        List<OutboxEntity> events = outboxRepository.findByProcessedFalseOrderByOccurredOnAsc();

        if (events.isEmpty()) {
            log.info("No unprocessed Payments Service outbox events found.");
            return;
        }

        for (OutboxEntity event : events) {
            try {
                Message<String> message = MessageBuilder
                        .withPayload(event.getPayload())
                        .setHeader(KafkaHeaders.TOPIC, "payment.processed")
                        .setHeader(KafkaHeaders.KEY, event.getId().toString())
                        .build();

                kafkaTemplate.send(message).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send Payments Service outbox event to Kafka: {}. Event will be retried.",
                                event.getId(), ex);
                    } else {
                        log.info("Successfully sent Payments Service outbox event to Kafka: {}", event.getId());
                        event.setProcessed(true);
                        outboxRepository.save(event);
                    }
                });
            } catch (Exception e) {
                log.error("Unexpected error while processing Payments Service outbox event: {}. Event will be retried.",
                        event.getId(), e);
            }
        }
        log.info("Finished processing Payments Service outbox events.");
    }
}