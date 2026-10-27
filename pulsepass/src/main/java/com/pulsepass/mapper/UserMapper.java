package com.pulsepass.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.pulsepass.domain.User;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;

@Mapper(componentModel ="spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "profile", ignore = true)
    User toEntity(RegisterUserRequest request);
    
    @Mapping(target = "firstName", source = "profile.firstName")
    @Mapping(target = "lastName", source = "profile.lastName")
    @Mapping(target = "phone", source = "profile.phone")
    @Mapping(target = "city", source = "profile.city")
    @Mapping(target = "birthDate", source = "profile.birthDate")
    UserResponse toResponse(User user);
    
}
