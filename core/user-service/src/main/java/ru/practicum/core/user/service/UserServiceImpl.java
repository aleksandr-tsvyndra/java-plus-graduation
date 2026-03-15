package ru.practicum.core.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.exception.DataAlreadyExistException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.user.dto.NewUserRequest;
import ru.practicum.core.user.dto.UserDto;
import ru.practicum.core.user.mapper.UserMapper;
import ru.practicum.core.user.model.User;
import ru.practicum.core.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.core.api.exception.NotFoundException.notFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private static final String EMAIL_ALREADY_EXISTS = "Пользователь с email %s уже существует";
    private static final String USER_NOT_FOUND = "Пользователь с ID %d не найден";

    @Override
    public UserDto createUser(NewUserRequest request) {
        String userEmail = request.getEmail();
        if (userRepository.existsByEmail(userEmail)) {
            throw new DataAlreadyExistException(String.format(EMAIL_ALREADY_EXISTS, userEmail));
        }

        User user = userMapper.toModel(request);
        User savedUser = userRepository.save(user);
        log.info("Создан пользователь: {} (email: {})", savedUser.getName(), userEmail);
        return userMapper.toDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        if (ids != null && !ids.isEmpty()) {
            log.info("Запрос пользователей по идентификаторам: {}", ids);
            return userRepository.findAllById(ids).stream()
                    .map(userMapper::toDto)
                    .collect(Collectors.toList());
        }

        int pageNumber = Math.floorDiv(from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        Page<User> userPage = userRepository.findAll(pageable);

        log.info("Запрошена страница {} с размером {}", pageNumber, size);
        return userPage.getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        User user = getUser(userId);
        try {
            log.info("Начало удаления пользователя: {} (ID: {})", user.getName(), userId);
            userRepository.deleteById(userId);
            log.info("Пользователь успешно удалён: {}", user.getName());
        } catch (Exception e) {
            log.error("Ошибка удаления пользователя {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Не удалось удалить пользователя", e);
        }
    }

    @Override
    public UserShortDto getUserById(Long userId) {
        User user = getUser(userId);
        log.info("Получен пользователь: {} (ID: {})", user.getName(), userId);
        return userMapper.toShortDto(user);
    }

    @Override
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        // Проверка на null и пустой список
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        // Получаем пользователей из БД
        List<User> users = userRepository.findAllById(ids);

        // Проверяем, что все пользователи были найдены
        if (users.size() != ids.size()) {
            throw new NotFoundException("Не все пользователи были найдены");
        }
        // Возвращаем список DTO краткой информации о пользователях
        return users.stream()
                .map(userMapper::toShortDto)
                .toList();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(notFoundException(USER_NOT_FOUND, userId));
    }
}