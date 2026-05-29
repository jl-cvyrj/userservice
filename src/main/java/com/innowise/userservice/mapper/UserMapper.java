package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);
    User toEntity(UserDto userDto);
}