package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.api.user.AdminUserApi;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminUserController implements AdminUserApi {
    private final UserService userService;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        return userService.getUsers(ids, PageRequest.of(from / size, size));
    }

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        return userService.createUser(newUserRequest);
    }

    @Override
    public void deleteUser(Long userId) {
        userService.deleteUser(userId);
    }

    @Override
    public UserDto getUserById(Long userId) {
        return userService.getUserById(userId);
    }

    @Override
    public UserShortDto getUserShortDtoById(Long userId) {
        return userService.getUserShortDtoById(userId);
    }
}