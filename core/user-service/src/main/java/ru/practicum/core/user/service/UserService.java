package ru.practicum.core.user.service;

import ru.practicum.core.api.exception.DataAlreadyExistException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.internal.user.dto.UserShortDto;
import ru.practicum.core.user.dto.NewUserRequest;
import ru.practicum.core.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(List<Long> ids, int offset, int limit);

    UserDto createUser(NewUserRequest request);

    void deleteUser(Long userId);

    UserShortDto getUserById(Long userId);

    List<UserShortDto> getUsersByIds(List<Long> ids);
}