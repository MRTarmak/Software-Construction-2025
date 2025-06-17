package hse.controller;

import hse.service.PaymentsService;
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
import java.util.UUID;

@Schema(name = "CommonErrorResponse", description = "Общий формат ответа для ошибок API")
record CommonErrorResponse(
        @Schema(description = "Временная метка ошибки", example = "2025-06-17T14:44:00.761512996")
        LocalDateTime timestamp,
        @Schema(description = "Сообщение об ошибке", example = "The account already exists for user: ...")
        String message,
        @Schema(description = "Дополнительные детали ошибки (могут отсутствовать)",
                example = "Internal server error details")
        String details
) {}

@Tag(name = "Управление счетами", description = "API для создания, пополнения и получения баланса учетных записей.")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class PaymentsController {
    private final PaymentsService paymentsService;

    @PostMapping("/create/{userId}")
    @Operation(summary = "Создать учетную запись",
            description = "Создает новую учетную запись для указанного пользователя с начальным нулевым балансом.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учетная запись успешно создана",
                    content = @Content(mediaType = "text/plain", schema = @Schema(
                            type = "string",
                            example = "The account was successfully created " +
                                    "for user: a1b2c3d4-e5f6-7890-1234-567890abcdef"
                    ))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class
                    )))
    })
    public ResponseEntity<String> createAccount(
            @Parameter(
                    description = "Уникальный идентификатор пользователя (UUID)",
                    required = true,
                    example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
            )
            @PathVariable UUID userId) {
        paymentsService.createAccount(userId);
        return ResponseEntity.ok("The account was successfully created for user: " + userId);
    }

    @PostMapping("/top-up/{userId}/{amount}")
    @Operation(summary = "Пополнить баланс",
            description = "Пополняет баланс учетной записи указанного пользователя на заданную сумму. Сумма должна быть положительной.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно пополнен",
                    content = @Content(mediaType = "text/plain", schema = @Schema(
                            type = "string",
                            example = "The balance of user a1b2c3d4-e5f6-7890-1234-567890abcdef " +
                                    "has been successfully increased by 100.50"
                    ))),
            @ApiResponse(responseCode = "400", description = "Неверный запрос (например, неверный формат UUID, отрицательная сумма)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CommonErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Учетная запись пользователя не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class
                    )))
    })
    public ResponseEntity<String> topUpBalance(
            @Parameter(
                    description = "Уникальный идентификатор пользователя (UUID)",
                    required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
            )
            @PathVariable UUID userId,
            @Parameter(
                    description = "Сумма для пополнения (должна быть положительной)",
                    required = true, example = "100.50"
            )
            @PathVariable BigDecimal amount) {
        paymentsService.topUpBalance(userId, amount);
        return ResponseEntity.ok("The balance of user " + userId + " has been successfully increased by " + amount);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Получить баланс",
            description = "Возвращает текущий баланс учетной записи указанного пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            type = "string", format = "double", example = "250.75"
                    ))),
            @ApiResponse(responseCode = "404", description = "Учетная запись пользователя не найдена",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class
                    ))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            implementation = CommonErrorResponse.class
                    )))
    })
    public ResponseEntity<BigDecimal> getBalance(
            @Parameter(
                    description = "Уникальный идентификатор пользователя (UUID)",
                    required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef"
            )
            @PathVariable UUID userId) {
        return ResponseEntity.ok(paymentsService.getBalance(userId));
    }
}