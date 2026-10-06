package com.pulsepass.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import com.pulsepass.domain.User;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.mapper.UserMapper;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock 
    private UserRepository userRepository;

    @Mock 
    private UserMapper userMapper;

    @InjectMocks 
    private UserServiceImpl userService;
    
    @Test 
    @DisplayName("TEST-USER-001: Registrar usuario valido debe guardar y retornar UserResponse")
    void register_ValidUser_ReturnsUserResponse(){
        RegisterUserRequest request = new RegisterUserRequest(
                "carlos21",
                "carlos@email.com",
                "Carlos",
                "Perez",
                "3001234567",
                "Santa Marta",
                LocalDate.of(2000, 5, 15)
        );

        User userEntity = new User();
        userEntity.setUsername(request.username());
        userEntity.setEmail(request.email());

        UserResponse expectedResponse = new UserResponse(
            1L,
            "carlos21",
            "carlos@email.com",
            "Carlos",
            "Perez",
            "3001234567",
            "Santa Marta",
            LocalDate.of(2000, 5, 15),
            true
        );

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(expectedResponse);


        UserResponse result = userService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("carlos21");
        assertThat(result.email()).isEqualTo("carlos@email.com");

        verify(userRepository).save(any(User.class));


    }

    @Test
    @DisplayName("TEST-USER-002: Username duplicado lanza DuplicateResourceException y no guarda")
    void register_DuplicateUsername_ThrowsDuplicateResourceException() {
        RegisterUserRequest request = new RegisterUserRequest(
                "carlos21",
                "carlos@email.com",
                "Carlos",
                "Perez",
                "3001234567",
                "Santa Marta",
                LocalDate.of(2000, 5, 15)
        );

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User already exists with username");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-USER-003: Email duplicado lanza DuplicateResourceException y no guarda")
    void register_DuplicateEmail_ThrowsDuplicateResourceException() {

        RegisterUserRequest request = new RegisterUserRequest(
                "carlos21",
                "carlos@email.com",
                "Carlos",
                "Perez",
                "3001234567",
                "Santa Marta",
                LocalDate.of(2000, 5, 15)
        );

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User already exists with email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST-USER-004: Birth date futura lanza BusinessRuleException y no valida repositorio")
    void register_FutureBirthDate_ThrowsBusinessRuleException() {
        RegisterUserRequest request = new RegisterUserRequest(
                "carlos21",
                "carlos@email.com",
                "Carlos",
                "Perez",
                "3001234567",
                "Santa Marta",
                LocalDate.now().plusDays(10) 
        );

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Birth date cannot be in the future");

        verify(userRepository, never()).findByUsername(any());
        verify(userRepository, never()).save(any());
    }

    
}
