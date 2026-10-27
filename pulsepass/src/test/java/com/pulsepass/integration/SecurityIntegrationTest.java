package com.pulsepass.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.pulsepass.controller.AuthController;
import com.pulsepass.controller.EventController;
import com.pulsepass.controller.TicketController;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.JwtService;
import com.pulsepass.security.RestAccessDeniedHandler;
import com.pulsepass.security.RestAuthenticationEntryPoint;
import com.pulsepass.security.SecurityConfig;
import com.pulsepass.service.AuthService;
import com.pulsepass.service.EventService;
import com.pulsepass.service.TicketService;

@WebMvcTest(controllers = {AuthController.class, EventController.class, TicketController.class})
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
class SecurityIntegrationTest {

   @Autowired
private WebApplicationContext context;

private MockMvc mockMvc;

@BeforeEach
void setUp() {
    mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
}

    @MockitoBean 
    private JwtService jwtService;

    @MockitoBean 
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean 
    private EventService eventService;

    @MockitoBean 
    private TicketService ticketService;


    @Test
    void tSec001_anonymousCanAccessPublishedEvents() throws Exception {
        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk());
    }

    @Test
    void tSec002_anonymousCannotCreateEvent() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventCode\":\"CMF-2026\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void tSec003_userCannotCreateEvent() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventCode\":\"CMF-2026\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void tSec006_loginWithInvalidPasswordReturns401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"andrea\",\"password\":\"WrongPassword!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void tSec008_userCannotPublishEvent() throws Exception {
        mockMvc.perform(patch("/api/events/CMF-2026/publish"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "1", roles = "USER")
    void tSec012_userCannotUseTicket() throws Exception {
        mockMvc.perform(patch("/api/tickets/TKT-12345/use"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}