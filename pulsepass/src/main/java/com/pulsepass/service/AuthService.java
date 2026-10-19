package com.pulsepass.service;

import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.domain.UserRole;
import com.pulsepass.domain.enums.Role;
import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.mapper.UserMapper;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import com.pulsepass.security.JwtProperties;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.SecurityPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final UserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            UserRepository userRepository,
            UserCredentialRepository credentialRepository,
            UserRoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.identifier(), request.password())
            );

            SecurityPrincipal principal = (SecurityPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);

            return new AuthResponse(token, 900);
        } catch (Exception e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        UserCredential credential = new UserCredential(savedUser, passwordEncoder.encode(request.password()));
        credentialRepository.save(credential);

        UserRole userRole = new UserRole(savedUser, Role.USER);
        roleRepository.save(userRole);

        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse getMe(SecurityPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<UserRole> roles = roleRepository.findByUserId(user.getId());
        Set<Role> roleEnums = roles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());

        return new AuthenticatedUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getActive(),
                roleEnums
        );
    }
}