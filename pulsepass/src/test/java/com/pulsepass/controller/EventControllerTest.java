package com.pulsepass.controller;

import com.pulsepass.domain.enums.EventCategory;
import com.pulsepass.domain.enums.EventStatus;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.security.CustomUserDetailsService;
import com.pulsepass.security.JwtService;
import com.pulsepass.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import tools.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(GlobalExceptionHandler.class)
class EventControllerTest {
        
        @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    @Test
    @DisplayName("TEST-CTRL-EVT-001: crear evento válido retorna 201 Created")
    void create_ShouldReturn201_WhenRequestIsValid() throws Exception {
        CreateEventRequest request = new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest 2026", "Festival de musica",
                EventCategory.MUSIC, LocalDateTime.now().plusDays(30), 18, "VEN-SMR-01"
        );

        EventResponse response = new EventResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival de musica",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.now().plusDays(30), 18, "VEN-SMR-01","Marina Convention Center", Set.of()
        );

        when(eventService.create(any(CreateEventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.name").value("Caribbean Music Fest 2026"));

        verify(eventService).create(any(CreateEventRequest.class));
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-002: request de creación inválido retorna 400 Bad Request")
    void create_ShouldReturn400_WhenRequestIsInvalid() throws Exception {
        CreateEventRequest invalidRequest = new CreateEventRequest(
                "", "", "Descripcion", null, null, -1, ""
        );

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.eventCode").exists())
                .andExpect(jsonPath("$.details.name").exists());

        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-003: consultar evento por código existente retorna 200 OK")
    void findByCode_ShouldReturn200_WhenExists() throws Exception {
        EventResponse response = new EventResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.now().plusDays(10), 18, "VEN-SMR-01", "Marina Convention Center", Set.of()
        );

        when(eventService.findByCode("CMF-2026")).thenReturn(response);

        mockMvc.perform(get("/api/events/CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        verify(eventService).findByCode("CMF-2026");
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-004: consultar evento inexistente retorna 404 Not Found")
    void findByCode_ShouldReturn404_WhenNotExists() throws Exception {
        when(eventService.findByCode("NOT-FOUND")).thenThrow(new ResourceNotFoundException("Event not found"));

        mockMvc.perform(get("/api/events/NOT-FOUND"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(eventService).findByCode("NOT-FOUND");
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-005: listar publicados -> 200")
    void findPublishedEvents_ShouldReturn200() throws Exception {
        EventSummaryResponse summary = new EventSummaryResponse(1L,
                "CMF-2026", "Caribbean Music Fest 2026", EventCategory.MUSIC,EventStatus.PUBLISHED,
                LocalDateTime.now().plusDays(10), "Marina Convention Center"
        );

        when(eventService.findPublishedEvents()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(eventService).findPublishedEvents();
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-006: publicar evento válido retorna 200 OK")
    void publish_ShouldReturn200_WhenValid() throws Exception {
        EventResponse response = new EventResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), 18, "VEN-SMR-01","Marina Convention Center", Set.of()
        );

        when(eventService.publish("CMF-2026")).thenReturn(response);

        mockMvc.perform(patch("/api/events/CMF-2026/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(eventService).publish("CMF-2026");
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-007: publicar evento con conflicto de regla retorna 409 Conflict")
    void publish_ShouldReturn409_WhenBusinessRuleFails() throws Exception {
        when(eventService.publish("CMF-2026")).thenThrow(new BusinessRuleException("Cannot publish event without venue capacity"));

        mockMvc.perform(patch("/api/events/CMF-2026/publish"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(eventService).publish("CMF-2026");
    }
    @Test
    @DisplayName("TEST-CTRL-EVT-008: asociar artista -> 200")
    void addArtist_ShouldReturn200() throws Exception {
        ArtistResponse artist = new ArtistResponse(1L, "Solar Beat", "ELECTRO", true);
        EventResponse response = new EventResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.now().plusDays(30),
                18, "VEN-SMR-01", "Marina Convention Center", Set.of(artist)
        );

        when(eventService.addArtist("CMF-2026", 1L)).thenReturn(response);

        mockMvc.perform(post("/api/events/CMF-2026/artists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists.length()").value(1));

        verify(eventService).addArtist("CMF-2026", 1L);
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-009: buscar por artista -> 200")
    void findByArtist_ShouldReturn200() throws Exception {
        EventSummaryResponse summary = new EventSummaryResponse(
                1L, "CMF-2026", "Caribbean Music Fest 2026", EventCategory.MUSIC,
                EventStatus.PUBLISHED, LocalDateTime.now().plusDays(10), "Marina Convention Center"
        );

        when(eventService.findByArtist("Solar Beat")).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(eventService).findByArtist("Solar Beat");
    }
}