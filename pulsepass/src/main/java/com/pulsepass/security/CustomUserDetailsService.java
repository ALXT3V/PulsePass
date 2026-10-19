package com.pulsepass.security;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.domain.UserRole;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final UserRoleRepository roleRepository;

    public CustomUserDetailsService(
            UserRepository userRepository,
            UserCredentialRepository credentialRepository,
            UserRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        UserCredential credential = credentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales no configuradas"));

        List<UserRole> roles = roleRepository.findByUserId(user.getId());

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRole().name()))
                .toList();

        return new SecurityPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                credential.getPasswordHash(),
                user.getActive(),
                authorities
        );
    }
}