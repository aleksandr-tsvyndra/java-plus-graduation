package ru.practicum.core.api.internal.user.client;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.core.api.internal.user.dto.UserShortDto;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class UserClientFallback implements UserClient {
    private static final String USER_FALLBACK_MSG = "Ошибка получения пользователя ID={}. Использован fallback.";
    private static final String USERS_FALLBACK_MSG = "Ошибка получения пользователей. Использован fallback.";

    @Override
    public ResponseEntity<UserShortDto> getUser(Long userId) throws FeignException {
        log.warn(USER_FALLBACK_MSG, userId);
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<List<UserShortDto>> getUsers(List<Long> ids) throws FeignException {
        log.warn(USERS_FALLBACK_MSG);
        return ResponseEntity.ok(Collections.emptyList());
    }
}