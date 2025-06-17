package hse.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentProcessedEvent(
        UUID orderId,
        UUID userId,
        BigDecimal amount,
        UUID paymentId,
        String paymentStatus,
        String reason // if failure
) {}