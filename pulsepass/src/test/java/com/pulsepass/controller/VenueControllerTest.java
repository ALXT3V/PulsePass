package com.pulsepass.controller;

import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.VenueService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VenueController.class)
@Import(GlobalExceptionHandler.class)

 class VenueControllerTest {

    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    @Test 
    @DisplayName("TEST-CTRL-VEN-001: venue existente retorna 200 OK")
    void findByCode_ShouldReturnVenue_WhenExists() throws Exception {
        VenueResponse response = new VenueResponse(1L, "VEN-SMR-01", "Marina Convention Center", "Santa Marta", 3, true);
        when(venueService.findByCode("VEN-SMR-01")).thenReturn(response);

        mockMvc.perform(get("/api/venues/VEN-SMR-01")
                            .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code").value("VEN-SMR-01"))
                        .andExpect(jsonPath("$.name").value("Marina Convention Center"));
                       

                        verify(venueService).findByCode("VEN-SMR-01");
    
    }

    @Test 
    @DisplayName("TEST-CTRL-VEN-002: venue inexistente retorna 404 Not Found")
    void findByCode_ShouldReturn404_WhenNotExists() throws Exception{
        when(venueService.findByCode("INVALID")).thenThrow(new ResourceNotFoundException("Venue not found: INVALID"));

        mockMvc.perform(get("/api/venues/INVALID")
                            .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.status").value(404))
                        .andExpect(jsonPath("$.error").value("Not Found"))
                        .andExpect(jsonPath("$.message").value("Venue not found: INVALID"));       
    
                        verify(venueService).findByCode("INVALID");
    }

    @Test 
    @DisplayName("TEST-CTRL-VEN-003: venues activos retorna 200 OK")
    void findActiveVenues_ShouldReturnList() throws Exception {
        VenueResponse response = new VenueResponse(1L, "VEN-SMR-01", "Marine Convention Center", "Santa Marta", 3, true);
        when(venueService.findActiveVenues()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/venues/active")
                            .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(1))
                        .andExpect(jsonPath("$[0].code").value("VEN-SMR-01"));
                        
                        verify(venueService).findActiveVenues();
    }


    
}
