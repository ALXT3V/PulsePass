package com.pulsepass.security;

import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.User;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    void verifyUserOwnershipShouldAllowSameUser() {
        SecurityPrincipal principal = new SecurityPrincipal(
                1L, "andrea", "andrea@test.com", "hash", true, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        assertThatCode(() -> authorizationService.verifyUserOwnership(principal, "andrea@test.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void verifyUserOwnershipShouldDenyDifferentUser() {
        SecurityPrincipal principal = new SecurityPrincipal(
                1L, "andrea", "andrea@test.com", "hash", true, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        assertThatThrownBy(() -> authorizationService.verifyUserOwnership(principal, "carlos@test.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void verifyTicketOwnershipShouldDenyThirdPartyUser() {
        SecurityPrincipal principal = new SecurityPrincipal(
                1L, "andrea", "andrea@test.com", "hash", true, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        User carlos = new User();
        carlos.setId(2L);
        Ticket ticket = new Ticket();
        ticket.setUser(carlos);

        when(ticketRepository.findByTicketCode("TKT-999")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> authorizationService.verifyTicketOwnership(principal, "TKT-999"))
                .isInstanceOf(AccessDeniedException.class);
    }
}