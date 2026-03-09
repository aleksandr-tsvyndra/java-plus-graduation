package ru.practicum.service;

import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers(List<Long> ids, Pageable pageable);

    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);

    UserDto getUserById(Long userId);

    UserShortDto getUserShortDtoById(Long userId);

}