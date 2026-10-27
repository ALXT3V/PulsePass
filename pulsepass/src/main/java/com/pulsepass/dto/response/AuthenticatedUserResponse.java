package com.pulsepass.dto.response;

import java.util.Set;

import com.pulsepass.domain.enums.Role;

public record AuthenticatedUserResponse(
    Long id,
    String username,
    String email,
    boolean active,
    Set<Role> roles
) {}
