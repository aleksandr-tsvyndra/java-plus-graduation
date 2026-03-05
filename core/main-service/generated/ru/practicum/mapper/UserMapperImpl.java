package ru.practicum.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.User;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-05T17:08:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toUserDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        return userDto;
    }

    @Override
    public UserShortDto toUserShortDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserShortDto userShortDto = new UserShortDto();

        return userShortDto;
    }

    @Override
    public User toUser(NewUserRequest newUserRequest) {
        if ( newUserRequest == null ) {
            return null;
        }

        User user = new User();

        return user;
    }
}
