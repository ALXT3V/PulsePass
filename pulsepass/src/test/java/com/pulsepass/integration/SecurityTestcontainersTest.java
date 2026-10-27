package com.pulsepass.integration;

import com.pulsepass.domain.User;
import com.pulsepass.domain.UserCredential;
import com.pulsepass.domain.UserRole;
import com.pulsepass.domain.enums.Role;
import com.pulsepass.repository.UserCredentialRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class SecurityTestcontainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("pulsepass_test_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository credentialRepository;

    @Autowired
    private UserRoleRepository roleRepository;

    @Test
    @Transactional
    void shouldPersistUserWithCredentialAndRoleInRealPostgres() {
        User user = new User();
        user.setUsername("andrea_real");
        user.setEmail("andrea_real@pulsepass.com");
        user.setActive(true);

        User savedUser = userRepository.save(user);

        UserCredential credential = new UserCredential(savedUser, "$2a$10$e8p.gXh2d6z8.8.8.8.8.8.8.8.8");
        credentialRepository.save(credential);

        UserRole userRole = new UserRole(savedUser, Role.USER);
        roleRepository.save(userRole);

        assertThat(userRepository.findById(savedUser.getId())).isPresent();
        assertThat(credentialRepository.findByUserId(savedUser.getId())).isPresent();
        assertThat(roleRepository.findByUserId(savedUser.getId())).hasSize(1);
    }
}