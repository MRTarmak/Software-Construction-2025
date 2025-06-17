package hse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hse.event.OrderCreatedEvent;
import hse.event.PaymentProcessedEvent;
import hse.exception.AccountNotFoundException;
import hse.exception.PaymentsException;
import hse.model.AccountEntity;
import hse.model.OutboxEntity;
import hse.repository.AccountRepository;
import hse.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentsService {
    private final AccountRepository accountRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createAccount(UUID userId) {
        if (accountRepository.findByUserId(userId).isPresent()) {
            throw new PaymentsException("The account already exists for user: " + userId);
        }

        AccountEntity account = AccountEntity.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .build();

        accountRepository.save(account);
    }

    @Transactional
    public void topUpBalance(UUID userId, BigDecimal amount) {
        AccountEntity account = accountRepository.findByUserId(userId).orElseThrow(() -> new AccountNotFoundException(
                "The account was not found for user: " + userId
        ));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The amount has to be positive");
        }

        account.setBalance(account.getBalance().add(amount));

        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID userId) {
        return accountRepository.findByUserId(userId)
                .map(AccountEntity::getBalance)
                .orElseThrow(() -> new AccountNotFoundException(
                        "The account was not found for user: " + userId
                ));
    }

    @Transactional
    public void processOrderPayment(OrderCreatedEvent event) throws JsonProcessingException {
        UUID orderId = event.id();
        UUID userId = event.userId();
        BigDecimal amount = event.amount();

        log.info("Processing OrderCreatedEvent for orderId: {}, userId: {}, amount: {}", orderId, userId, amount);

        boolean success = false;
        String failureReason = null;

        try {
            Optional<AccountEntity> account = accountRepository.findByUserId(userId);
            if (account.isEmpty()) {
                failureReason = "ACCOUNT_NOT_FOUND";
            } else if (account.get().getBalance().compareTo(amount) < 0) {
                failureReason = "INSUFFICIENT_FUNDS";
            } else {
                account.get().setBalance(account.get().getBalance().subtract(amount));
                accountRepository.save(account.get());
                success = true;
            }
        } catch (Exception e) {
            log.error("Error during payment processing for orderId {}: {}", orderId, e.getMessage(), e);
            failureReason = "INTERNAL_ERROR";
        }

        try {
            if (success) {
                log.info("Payment successful for orderId: {}", orderId);
                String payload = objectMapper.writeValueAsString(new PaymentProcessedEvent(
                        orderId,
                        userId,
                        amount,
                        UUID.randomUUID(),
                        "SUCCESS",
                        null
                ));
                saveOutboxEvent(orderId, "PAYMENT_SUCCESS", payload);
            } else {
                log.warn("Payment failed for orderId: {}. Reason: {}", orderId, failureReason);
                String payload = objectMapper.writeValueAsString(new PaymentProcessedEvent(
                        orderId,
                        userId,
                        amount,
                        UUID.randomUUID(),
                        "FAILURE",
                        failureReason
                ));
                saveOutboxEvent(orderId, "PAYMENT_FAILED", payload);
            }
        } catch (JsonProcessingException e) {
            throw new PaymentsException("Event serialization error", e);
        }
    }

    private void saveOutboxEvent(UUID aggregateId, String eventType, String payload) {
        OutboxEntity outbox = OutboxEntity.builder()
                .aggregateType("Payment")
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .processed(false)
                .build();
        outboxRepository.save(outbox);
    }
}