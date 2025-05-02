package ru.maverick.cardmanagementsystem.card.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.maverick.cardmanagementsystem.card.dto.CardDto;
import ru.maverick.cardmanagementsystem.card.mapper.CardMapper;
import ru.maverick.cardmanagementsystem.card.model.enums.CardStatus;
import ru.maverick.cardmanagementsystem.card.request.CardCreateRequest;
import ru.maverick.cardmanagementsystem.card.request.TransferAmountRequest;
import ru.maverick.cardmanagementsystem.card.service.CardService;
import ru.maverick.cardmanagementsystem.user.model.User;

import java.util.UUID;

@Slf4j
@Tag(
        name = "Cards",
        description = "Управление банковскими картами. Требует аутентификации."
)
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    // -------------------- User Endpoints --------------------

    @Operation(
            summary = "Перевод между своими картами",
            description = "Перевод средств между двумя картами пользователя. "
                    + "Карты должны быть активны и принадлежать текущему пользователю.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный запрос: недостаточно средств/некорректная сумма"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен: карта не принадлежит пользователю"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена"
            )
    })
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transferFunds(
            @Valid @RequestBody TransferAmountRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.info("Received transferFunds. fromCardNumber={}, toCardNumber={}, amount={}, userId={}",
                request.getFromCardNumber(), request.getToCardNumber(), request.getAmount(), user.getId());
        cardService.transferFundsByCardNumber(
                request.getFromCardNumber(),
                request.getToCardNumber(),
                request.getAmount(),
                user
        );
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Получить свои карты",
            description = "Возвращает список карт текущего пользователя с возможностью фильтрации по статусу.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешный запрос",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardDto>> getUserCards(
        @AuthenticationPrincipal User requester,
        @RequestParam(required = false) CardStatus cardStatus,
        Pageable pageable
    ) {
        log.info("Received getUserCards. userId={}, cardStatus={}", requester.getId(), cardStatus);
        return ResponseEntity.ok(
                cardService.getUserCards(requester, cardStatus, pageable)
                        .map(cardMapper::toDto)
        );
    }

    @Tag(name="BlockRequests", description="Запросы на блокировку карт")
    @Operation(
            summary = "Запрос блокировки карты",
            description = "Создает запрос на блокировку карты. Только для владельца карты.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Запрос успешно создан"),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен: карта не принадлежит пользователю"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена"
            )
    })
    @PostMapping("/block-requests")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> requestBlock(
            @Parameter(description = "Номер карты", example = "123456******7890")
            @RequestParam String cardNumber,
            @AuthenticationPrincipal User user
    ) {
        log.info("Received requestBlock. cardNumber={}, userId={}", cardNumber, user.getId());
        cardService.createCardBlockRequest(cardNumber, user);
        return ResponseEntity.ok().build();
    }

    // -------------------- Admin Endpoints --------------------

    @Operation(
            summary = "Создать новую карту",
            description = "Создает новую карту. Требуются права администратора.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Карта успешно создана",
                    content = @Content(schema = @Schema(implementation = CardDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные карты"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(
            @Valid @RequestBody CardCreateRequest request
    ) {
        log.info("Received createCard. userId={}, expirationDate={}",
                request.getUserId(), request.getExpirationDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                cardMapper.toDto(cardService.createCard(request))
        );
    }

    @Operation(
            summary = "Обновить статус карты",
            description = "Изменяет статус карты (активна/заблокирована). Требуются права администратора.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус успешно обновлен"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена"
            )
    })
    @PatchMapping("/{cardId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateCardStatus(
            @Parameter(description = "UUID карты", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID cardId,
            @RequestParam CardStatus status
    ) {
        log.info("Received updateCardStatus. cardId={}, status={}", cardId, status);
        cardService.updateCardStatus(cardId, status);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Получить информацию о карте",
            description = "Возвращает полную информацию о карте. Требуются права администратора.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный запрос",
                    content = @Content(schema = @Schema(implementation = CardDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена"
            )
    })
    @GetMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> getCard(
            @Parameter(description = "UUID карты", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID cardId
    ) {
        return ResponseEntity.ok(cardMapper.toDto(cardService.getCardById(cardId)));
    }

    @Operation(
            summary = "Получить все карты",
            description = "Возвращает список всех карт с фильтрацией по статусу. Требуются права администратора.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный запрос",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getAllCards(
            @Parameter(description = "Фильтр по статусу", example = "ACTIVE")
            @RequestParam(required = false) CardStatus status,
            Pageable pageable
    ) {
        log.info("Received getAllCards. status={}", status);
        return ResponseEntity.ok(
                cardService.getAllCards(status, pageable)
                        .map(cardMapper::toDto)
        );
    }

    @Operation(
            summary = "Удалить карту",
            description = "Полностью удаляет карту из системы. Требуются права администратора.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена"
            )
    })
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "UUID карты", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID cardId
    ) {
        log.info("Received deleteCard. cardId={}", cardId);
        cardService.deleteCardById(cardId);
        return ResponseEntity.noContent().build();
    }

    @Tag(name="BlockRequests", description="Запросы на блокировку карт")
    @Operation(
        summary = "Одобрить блокировку карты",
        description = "Подтверждает запрос на блокировку карты. Требуются права администратора.",
        security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Блокировка подтверждена"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Запрос не найден"
            )
    })
    @PatchMapping("/block-requests/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveBlockRequest(
            @Parameter(description = "UUID запроса", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID requestId
    ) {
        log.info("Received approveBlockRequest. requestId={}", requestId);
        cardService.approveCardBlockRequest(requestId);
        return ResponseEntity.ok().build();
    }
}