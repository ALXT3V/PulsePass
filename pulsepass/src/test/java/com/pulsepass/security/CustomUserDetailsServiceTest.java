package com.pulsepass.security;

import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.domain.UserRole;
import com.pulsepass.domain.enums.Role;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCredentialRepository credentialRepository;
    @Mock
    private UserRoleRepository roleRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsernameShouldReturnSecurityPrincipal() {
        User user = new User();
        user.setUsername("andrea");
        user.setEmail("andrea@test.com");
        user.setActive(true);

        UserCredential credential = new UserCredential(user, "hashedPassword");
        UserRole userRole = new UserRole(user, Role.USER);

        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(user));
        when(credentialRepository.findByUserId(user.getId())).thenReturn(Optional.of(credential));
        when(roleRepository.findByUserId(user.getId())).thenReturn(List.of(userRole));

        UserDetails userDetails = userDetailsService.loadUserByUsername("andrea");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("andrea");
        assertThat(userDetails.getAuthorities()).hasSize(1);
    }

    @Test
    void loadUserByUsernameNotFoundShouldThrowException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuario no encontrado");
    }
}
