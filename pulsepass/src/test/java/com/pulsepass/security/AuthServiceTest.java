package com.pulsepass.security;

import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.domain.UserRole;
import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.mapper.UserMapper;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import com.pulsepass.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCredentialRepository credentialRepository;
    @Mock
    private UserRoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    private JwtProperties jwtProperties;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setIssuer("pulsepass-api-test");
        jwtProperties.setSecretBase64("c3VwZXItc2VjcmV0LWtleS1mb3ItcHVsc2VwYXNzLWFwaS1zZWN1cml0eS0yMDI2");
        jwtProperties.setAccessTokenTtl(Duration.ofMinutes(15));

        authService = new AuthService(
                authenticationManager,
                jwtService,
                jwtProperties,
                userRepository,
                credentialRepository,
                roleRepository,
                passwordEncoder,
                userMapper
        );
    }

    @Test
    void loginSuccessShouldReturnToken() {
        LoginRequest request = new LoginRequest("andrea", "Password123456!");
        SecurityPrincipal principal = new SecurityPrincipal(1L, "andrea", "andrea@test.com", "hash", true, List.of());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(principal, null);

        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(jwtService.generateToken(principal)).thenReturn("mocked.jwt.token");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.expiresIn()).isEqualTo(900);
    }

    @Test
    void loginFailureShouldThrowBadCredentialsException() {
        LoginRequest request = new LoginRequest("andrea", "WrongPassword!");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");
    }

@Test
    void registerShouldSaveUserCredentialAndUserRole() {
        RegisterUserRequest request = new RegisterUserRequest(
                "andrea", "andrea@test.com", "Password123456!", "Andrea", "Gomez", "3001234567", "Santa Marta", LocalDate.of(1995, 5, 10)
        );

        User savedUser = new User();
        savedUser.setUsername("andrea");

        UserResponse userResponse = new UserResponse(
                1L, "andrea", "andrea@test.com", "Andrea", "Gomez", "3001234567", "Santa Marta", LocalDate.of(1995, 5, 10), true
        );

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedHash");
        when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

        UserResponse response = authService.register(request);

        assertThat(response).isNotNull();
        verify(credentialRepository).save(any(UserCredential.class));
        verify(roleRepository).save(any(UserRole.class));
    }
}