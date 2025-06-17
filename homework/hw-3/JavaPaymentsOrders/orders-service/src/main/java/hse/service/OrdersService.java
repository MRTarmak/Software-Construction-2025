package hse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hse.dto.OrderDto;
import hse.event.OrderCreatedEvent;
import hse.event.PaymentProcessedEvent;
import hse.exception.OrderNotFoundException;
import hse.exception.OrdersException;
import hse.model.OrderEntity;
import hse.model.OrderStatus;
import hse.model.OutboxEntity;
import hse.repository.OrderRepository;
import hse.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrdersService {
    private final OrderRepository orderRepository;

    private final OutboxRepository outboxRepository;

    private final ObjectMapper objectMapper;


    public OrdersService(OrderRepository orderRepository,
                         OutboxRepository outboxRepository,
                         ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderDto createOrder(UUID userId, BigDecimal amount, String description) {
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Illegal data");
        }

        UUID orderId;

        boolean idExists;
        do {
            orderId = UUID.randomUUID();
            idExists = orderRepository.existsById(orderId);
        } while (idExists);

        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .userId(userId)
                .amount(amount)
                .description(description)
                .status(OrderStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();

        OrderEntity savedOrder = orderRepository.save(order);

        try {
            String payload = objectMapper.writeValueAsString(new OrderCreatedEvent(
                    savedOrder.getId(),
                    savedOrder.getUserId(),
                    savedOrder.getAmount()
            ));
            OutboxEntity outbox = OutboxEntity.builder()
                    .aggregateId(savedOrder.getId())
                    .aggregateType("Order")
                    .eventType("ORDER_CREATED")
                    .payload(payload)
                    .processed(false)
                    .build();
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new OrdersException("Event serialization error", e);
        }

        return OrderDto.builder()
                .id(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .amount(savedOrder.getAmount())
                .description(savedOrder.getDescription())
                .status(savedOrder.getStatus())
                .createdAt(savedOrder.getCreatedAt())
                .updatedAt(savedOrder.getUpdatedAt())
                .build();
    }

    @Transactional
    public List<OrderDto> viewOrdersList() {
        return orderRepository.findAll().stream().map(order -> OrderDto.builder()
                    .id(order.getId())
                    .userId(order.getUserId())
                    .amount(order.getAmount())
                    .description(order.getDescription())
                    .status(order.getStatus())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .build()
        ).toList();
    }

    @Transactional
    public List<OrderDto> viewOrdersListByUserId(UUID userId) {
        return orderRepository.findByUserId(userId).stream().map(order -> OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .amount(order.getAmount())
                .description(order.getDescription())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build()
        ).toList();
    }

    @Transactional
    public OrderStatus viewOrderStatus(UUID id) {
        OrderEntity order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(
                "The order was not found with id: " + id
        ));

        return order.getStatus();
    }

    @Transactional
    public void updateOrderStatus(PaymentProcessedEvent event, boolean finished) {
        orderRepository.findById(event.orderId()).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.NEW) {
                order.setStatus(finished ? OrderStatus.FINISHED : OrderStatus.CANCELLED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
            }
        });
    }
}