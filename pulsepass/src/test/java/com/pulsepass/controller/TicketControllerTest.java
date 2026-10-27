package com.pulsepass.controller;

import tools.jackson.databind.ObjectMapper;
import com.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.domain.enums.TicketType;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.JwtService;
import com.pulsepass.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketControllerTest {

 @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    @Test
    @DisplayName("TEST-CTRL-TKT-001: compra válida -> 201")
    void purchase_ShouldReturn201_WhenValid() throws Exception {
        PurchaseTicketRequest request = new PurchaseTicketRequest("andrea@gmail.com", "CMF-2026", TicketType.VIP);
        TicketResponse response = new TicketResponse(1L, "TKT-1001", TicketType.VIP, new BigDecimal("150000"), TicketStatus.PAID, LocalDateTime.now(), "andrea@gmail.com", "CMF-2026", "Caribbean Music Fest 2026");

        when(ticketService.purchase(any(PurchaseTicketRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketCode").value("TKT-1001"));

        verify(ticketService).purchase(any(PurchaseTicketRequest.class));
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-002: request inválido -> 400")
    void purchase_ShouldReturn400_WhenInvalid() throws Exception {
        PurchaseTicketRequest invalidRequest = new PurchaseTicketRequest("", "", null);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(ticketService, never()).purchase(any());
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-003: usuario inexistente -> 404")
    void purchase_ShouldReturn404_WhenUserNotFound() throws Exception {
        PurchaseTicketRequest request = new PurchaseTicketRequest("notfound@gmail.com", "CMF-2026", TicketType.VIP);

        when(ticketService.purchase(any(PurchaseTicketRequest.class))).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(ticketService).purchase(any(PurchaseTicketRequest.class));
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-004: regla de negocio -> 409")
    void purchase_ShouldReturn409_WhenBusinessRuleFails() throws Exception {
        PurchaseTicketRequest request = new PurchaseTicketRequest("laura17@gmail.com", "CMF-2026", TicketType.VIP);

        when(ticketService.purchase(any(PurchaseTicketRequest.class))).thenThrow(new BusinessRuleException("Underage user"));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(ticketService).purchase(any(PurchaseTicketRequest.class));
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-005: consultar ticket -> 200")
    void findByCode_ShouldReturn200() throws Exception {
        TicketResponse response = new TicketResponse(1L, "TKT-1001", TicketType.VIP, new BigDecimal("150000"), TicketStatus.PAID, LocalDateTime.now(), "andrea@gmail.com", "CMF-2026", "Caribbean Music Fest 2026");

        when(ticketService.findByCode("TKT-1001")).thenReturn(response);

        mockMvc.perform(get("/api/tickets/TKT-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketCode").value("TKT-1001"));

        verify(ticketService).findByCode("TKT-1001");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-006: tickets por usuario -> 200")
    void findByUserEmail_ShouldReturn200() throws Exception {
        TicketResponse response = new TicketResponse(1L, "TKT-1001", TicketType.VIP, new BigDecimal("150000"), TicketStatus.PAID, LocalDateTime.now(), "andrea@gmail.com", "CMF-2026", "Caribbean Music Fest 2026");

        when(ticketService.findByUserEmail("andrea@gmail.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/tickets/by-user").param("email", "andrea@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(ticketService).findByUserEmail("andrea@gmail.com");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-007: tickets PAID por evento -> 200")
    void findPaidTicketsByEvent_ShouldReturn200() throws Exception {
        TicketResponse response = new TicketResponse(1L, "TKT-1001", TicketType.VIP, new BigDecimal("150000"), TicketStatus.PAID, LocalDateTime.now(), "andrea@gmail.com", "CMF-2026", "Caribbean Music Fest 2026");

        when(ticketService.findPaidTicketsByEvent("CMF-2026")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/events/CMF-2026/tickets/paid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(ticketService).findPaidTicketsByEvent("CMF-2026");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-008: cancelar válido -> 200")
    void cancel_ShouldReturn200_WhenValid() throws Exception {
        TicketResponse response = new TicketResponse(1L, "TKT-1001", TicketType.VIP, new BigDecimal("150000"), TicketStatus.CANCELLED, LocalDateTime.now(), "andrea@gmail.com", "CMF-2026", "Caribbean Music Fest 2026");

        when(ticketService.cancel("TKT-1001")).thenReturn(response);

        mockMvc.perform(patch("/api/tickets/TKT-1001/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(ticketService).cancel("TKT-1001");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-009: cancelar inválido -> 409")
    void cancel_ShouldReturn409_WhenInvalid() throws Exception {
        when(ticketService.cancel("TKT-1001")).thenThrow(new BusinessRuleException("Cannot cancel non-PAID ticket"));

        mockMvc.perform(patch("/api/tickets/TKT-1001/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(ticketService).cancel("TKT-1001");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-010: usar válido -> 200")
    void markAsUsed_ShouldReturn200_WhenValid() throws Exception {
        TicketResponse response = new TicketResponse(1L, "TKT-1001", TicketType.VIP, new BigDecimal("150000"), TicketStatus.USED, LocalDateTime.now(), "andrea@gmail.com", "CMF-2026", "Caribbean Music Fest 2026");

        when(ticketService.markAsUsed("TKT-1001")).thenReturn(response);

        mockMvc.perform(patch("/api/tickets/TKT-1001/use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("USED"));

        verify(ticketService).markAsUsed("TKT-1001");
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-011: usar inválido -> 409")
    void markAsUsed_ShouldReturn409_WhenInvalid() throws Exception {
        when(ticketService.markAsUsed("TKT-1001")).thenThrow(new BusinessRuleException("Ticket is CANCELLED"));

        mockMvc.perform(patch("/api/tickets/TKT-1001/use"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(ticketService).markAsUsed("TKT-1001");
    }
}