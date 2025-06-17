package hse.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID id,
        UUID userId,
        BigDecimal amount
) {}
