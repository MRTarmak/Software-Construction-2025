package hse.controller;

import hse.dto.OrderDto;
import hse.model.OrderStatus;
import hse.service.OrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(name = "CommonErrorResponse", description = "Общий формат ответа для ошибок API")
record CommonErrorResponse(
        @Schema(description = "Временная метка ошибки", example = "2025-06-17T14:44:00.761512996")
        LocalDateTime timestamp,
        @Schema(description = "Сообщение об ошибке", example = "Illegal data")
        String message,
        @Schema(description = "Дополнительные детали ошибки (могут отсутствовать)",
                example = "Internal server error details")
        String details
) {}

@Tag(name = "Управление заказами", description = "API для создания, просмотра и управления заказами.")
@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrdersController {
    private final OrdersService ordersService;

    @PostMapping("/create/{userId}/{amount}/{description}")
    @Operation(summary = "Создать новый заказ",
            description = "Создает новый заказ для указанного пользователя с заданной суммой и описанием. " +
                    "Заказ изначально имеет статус NEW.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = OrderDto.class
                    ))),
            @ApiResponse(responseCode = "400", description = "Неверный запрос (например, неверный формат UUID, " +
                    "отрицательная сумма, пустое описание)",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class
                    ))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера (например, ошибка " +
                    "сериализации события, проблема с БД)",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class
                    )))
    })
    public ResponseEntity<OrderDto> createOrder(
            @Parameter(
                    description = "Уникальный идентификатор пользователя",
                    required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
            )
            @PathVariable UUID userId,
            @Parameter(
                    description = "Сумма заказа (должна быть положительной)",
                    required = true, example = "150.75"
            )
            @PathVariable BigDecimal amount,
            @Parameter(
                    description = "Описание заказа",
                    example = "Покупка книги 'Spring Boot in Action'"
            )
            @PathVariable String description) {
        return ResponseEntity.ok(ordersService.createOrder(userId, amount, description));
    }

    @GetMapping("/view")
    @Operation(summary = "Просмотреть список всех заказов",
            description = "Возвращает список всех заказов в системе.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = OrderDto.class
                    ))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class
                    )))
    })
    public ResponseEntity<List<OrderDto>> viewOrdersList() {
        return ResponseEntity.ok(ordersService.viewOrdersList());
    }

    @GetMapping("/view/{userId}")
    @Operation(summary = "Просмотреть список заказов по ID пользователя",
            description = "Возвращает список заказов, связанных с конкретным пользователем.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов по ID пользователя успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = OrderDto.class
                    ))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class
                    )))
    })
    public ResponseEntity<List<OrderDto>> viewOrdersListByUserId(
            @Parameter(
                    description = "Уникальный идентификатор пользователя",
                    required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
            )
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ordersService.viewOrdersListByUserId(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Просмотреть статус заказа по ID",
            description = "Возвращает текущий статус конкретного заказа.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус заказа успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = OrderStatus.class
                    ))),
            @ApiResponse(responseCode = "404", description = "Заказ не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class
                    ))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class
                    )))
    })
    public ResponseEntity<OrderStatus> viewOrderStatus(
            @Parameter(
                    description = "Уникальный идентификатор заказа",
                    required = true, example = "b5c6d7e8-f9a0-1122-3344-5566778899aa"
            )
            @PathVariable UUID id) {
        return ResponseEntity.ok(ordersService.viewOrderStatus(id));
    }
}