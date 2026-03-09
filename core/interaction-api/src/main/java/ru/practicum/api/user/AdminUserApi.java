package ru.practicum.api.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;

@Validated
public interface AdminUserApi {

    @GetMapping("/admin/users")
    List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                           @RequestParam(defaultValue = "0") int from,
                           @RequestParam(defaultValue = "10") int size);

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    UserDto createUser(@RequestBody @Valid NewUserRequest newUserRequest);

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@PathVariable @Positive Long userId);

    @GetMapping("/admin/users/{userId}")
    UserDto getUserById(@PathVariable @Positive Long userId);

    @GetMapping("/admin/users/client/{userId}")
    UserShortDto getUserShortDtoById(@PathVariable @Positive Long userId);

}
