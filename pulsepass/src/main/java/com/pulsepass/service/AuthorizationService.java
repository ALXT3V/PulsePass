package com.pulsepass.service;

import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.User;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.security.SecurityPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorizationService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public AuthorizationService(UserRepository userRepository, TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public void verifyUserOwnership(SecurityPrincipal principal, String email) {
        if (hasAdminRole(principal)) {
            return;
        }

        if (!principal.getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("No tiene permisos para operar sobre este recurso de usuario");
        }
    }

    @Transactional(readOnly = true)
    public void verifyUserOwnership(SecurityPrincipal principal, Long targetUserId) {
        if (hasAdminRole(principal)) {
            return;
        }

        if (!principal.getUserId().equals(targetUserId)) {
            throw new AccessDeniedException("No tiene permisos para acceder a los datos de otro usuario");
        }
    }

    @Transactional(readOnly = true)
    public void verifyTicketOwnership(SecurityPrincipal principal, String ticketCode) {
        if (hasAdminRole(principal) || hasStaffRole(principal)) {
            return;
        }

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new AccessDeniedException("Ticket no encontrado o no autorizado"));

        User owner = ticket.getUser();
        if (owner == null || !owner.getId().equals(principal.getUserId())) {
            throw new AccessDeniedException("No es el propietario de esta entrada");
        }
    }

    private boolean hasAdminRole(SecurityPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean hasStaffRole(SecurityPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
    }
}