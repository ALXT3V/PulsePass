package com.pulsepass.controller;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.ArtistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ArtistController.class)
@Import(GlobalExceptionHandler.class)
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistService artistService;

    @Test
    @DisplayName("TEST-CTRL-ART-001: buscar por ID -> 200")
    void findById_ShouldReturn200_WhenExists() throws Exception {
        ArtistResponse response = new ArtistResponse(1L, "Solar Beat", "ELECTRO", true);
        when(artistService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/artists/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(artistService).findById(1L);
    }

    @Test
    @DisplayName("TEST-CTRL-ART-002: ID inexistente -> 404")
    void findById_ShouldReturn404_WhenNotExists() throws Exception {
        when(artistService.findById(99L)).thenThrow(new ResourceNotFoundException("Artist not found"));

        mockMvc.perform(get("/api/artists/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(artistService).findById(99L);
    }

    @Test
    @DisplayName("TEST-CTRL-ART-003: buscar por stageName -> 200")
    void findByStageName_ShouldReturn200() throws Exception {
        ArtistResponse response = new ArtistResponse(1L, "Solar Beat", "ELECTRO", true);
        when(artistService.findByStageName("Solar Beat")).thenReturn(response);

        mockMvc.perform(get("/api/artists/by-stage-name")
                        .param("stageName", "Solar Beat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(artistService).findByStageName("Solar Beat");
    }

    @Test
    @DisplayName("TEST-CTRL-ART-004: listar activos -> 200")
    void findActiveArtists_ShouldReturn200() throws Exception {
        ArtistResponse response = new ArtistResponse(1L, "Solar Beat", "ELECTRO", true);
        when(artistService.findActiveArtists()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/artists/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stageName").value("Solar Beat"));

        verify(artistService).findActiveArtists();
    }
}