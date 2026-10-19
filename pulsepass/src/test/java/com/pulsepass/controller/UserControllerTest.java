package com.pulsepass.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.JwtService;
import com.pulsepass.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("TEST-CTRL-USR-001: registrar usuario válido retorna 201 Created")
    void register_ShouldReturn201_WhenValid() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                "andrea_m", "andrea@gmail.com","Password123456!", "Andrea", "Mendoza",
                "3001234567", "Santa Marta", LocalDate.of(1995, 5, 20)
        );

        UserResponse response = new UserResponse(
                1L, "andrea_m", "andrea@gmail.com", "Andrea", "Mendoza", "3001234567", "Santa Marta", LocalDate.of(1995, 5, 20), true
        );

        when(userService.register(any(RegisterUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("andrea_m"))
                .andExpect(jsonPath("$.email").value("andrea@gmail.com"));

        verify(userService).register(any(RegisterUserRequest.class));
    }

    @Test
    @DisplayName("TEST-CTRL-USR-002: email inválido -> 400")
    void register_ShouldReturn400_WhenEmailInvalid() throws Exception {
        RegisterUserRequest invalidRequest = new RegisterUserRequest(
                "andrea_m", "email-invalido", "Password123456!", "Andrea", "Mendoza",
                "3001234567", "Santa Marta", LocalDate.of(1995, 5, 20)
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.email").exists());

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("TEST-CTRL-USR-003: registrar usuario con username o email duplicado retorna 409 Conflict")
    void register_ShouldReturn409_WhenDuplicate() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                "andrea_m", "andrea@gmail.com","Password123456!", "Andrea", "Mendoza",
                "3001234567", "Santa Marta", LocalDate.of(1995, 5, 20)
        );

        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException("User already exists with email: andrea@gmail.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(userService).register(any(RegisterUserRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("TEST-CTRL-USR-004: consultar por email -> 200")
    void findByEmail_ShouldReturn200() throws Exception {
        UserResponse response = new UserResponse(
                1L, "andrea_m", "andrea@gmail.com", "Andrea", "Mendoza",
                "3001234567", "Santa Marta", LocalDate.of(1995, 5, 20), true
        );

        when(userService.findByEmail("andrea@gmail.com")).thenReturn(response);

        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("andrea@gmail.com"));

        verify(userService).findByEmail("andrea@gmail.com");
    }

    @Test
    @WithMockUser
    @DisplayName("TEST-CTRL-USR-005: consultar por username -> 200")
    void findByUsername_ShouldReturn200() throws Exception {
        UserResponse response = new UserResponse(
                1L, "andrea_m", "andrea@gmail.com", "Andrea", "Mendoza",
                "3001234567", "Santa Marta", LocalDate.of(1995, 5, 20), true
        );

        when(userService.findByUsername("andrea_m")).thenReturn(response);

        mockMvc.perform(get("/api/users/by-username").param("username", "andrea_m"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("andrea_m"));

        verify(userService).findByUsername("andrea_m");
    }
}
