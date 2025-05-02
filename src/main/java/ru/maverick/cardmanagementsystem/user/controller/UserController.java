package ru.maverick.cardmanagementsystem.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.maverick.cardmanagementsystem.user.dto.UserDto;
import ru.maverick.cardmanagementsystem.user.mapper.UserMapper;
import ru.maverick.cardmanagementsystem.user.model.User;
import ru.maverick.cardmanagementsystem.user.model.enums.Role;
import ru.maverick.cardmanagementsystem.user.request.UpdateUserRequest;
import ru.maverick.cardmanagementsystem.user.service.UserService;

@Tag(name = "User", description = "Управление пользователями")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(
            summary = "Получение всех пользователей",
            description = "Позволяет получить всех пользователей с пагинацией и сортировкой по id. Доступно только для администраторов.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Пользователи успешно получены",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "401", description = "Необходима аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа")
    })
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) Role role
    ) {
        log.info("Received getAllUsers. page={}, size={}, sort={}, role={}", page, size, sort, role);
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        Page<User> users = userService.getAllUsers(pageable, role);
        return ResponseEntity.ok(users.map(userMapper::toDto));
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает данные о пользователе по указанному ID. Доступно для администратора или самого пользователя.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Пользователь успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Integer userId) {
        log.info("Received getUserById. userId={}", userId);
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @Operation(
            summary = "Получить пользователя по email",
            description = "Возвращает данные о пользователе по указанному email. Доступно для администратора.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Пользователь успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/by-email")
    public ResponseEntity<UserDto> getUserByEmail(@RequestParam String email) {
        log.info("Received getUserByEmail. email={}", email);
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(userMapper.toDto(user));
    }



    @Operation(
            summary = "Обновить пользователя",
            description = "Обновляет данные пользователя по указанному ID. Доступно для администратора или самого пользователя.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Пользователь успешно обновлен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Integer userId,
            @RequestBody UpdateUserRequest request
            ) {
        log.info("Received updateUser. userId={}, payload={}", userId, request);
        User updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @Operation(
            summary = "Удалить пользователя",
            description = "Позволяет удалить пользователя по id. Доступно только для самого пользователя или администраторов.",
            security = @SecurityRequirement(name = "Bearer Token Auth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204", description = "Пользователь успешно удален",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "401", description = "Необходима аутентификация"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        log.info("Received deleteUser. userId={}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}