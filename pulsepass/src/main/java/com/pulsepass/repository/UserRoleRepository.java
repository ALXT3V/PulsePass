package com.pulsepass.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsepass.domain.UserRole;
import com.pulsepass.domain.UserRoleId;
import com.pulsepass.domain.enums.Role;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUserId(Long userId);
    boolean existsByUserIdAndIdRole(Long userId, Role role);
}