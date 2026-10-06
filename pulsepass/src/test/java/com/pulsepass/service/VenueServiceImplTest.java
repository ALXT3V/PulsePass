package com.pulsepass.service;

import com.pulsepass.domain.Venue;
import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.VenueMapper;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.impl.VenueServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private VenueMapper venueMapper;

    @InjectMocks
    private VenueServiceImpl venueService;

    @Test
    @DisplayName("TEST-VENUE-001: Buscar venue por codigo existente retorna DTO")
    void findByCode_ExistingCode_ReturnsVenueResponse() {
        String venueCode = "VEN-SMR-01";
        Venue venue = new Venue();
        venue.setCode(venueCode);

        VenueResponse expectedResponse = new VenueResponse(1L, venueCode, "Marina Convention Center", "Santa Marta", 500, true);

        when(venueRepository.findByCode(venueCode)).thenReturn(Optional.of(venue));
        when(venueMapper.toResponse(venue)).thenReturn(expectedResponse);

        VenueResponse result = venueService.findByCode(venueCode);

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo(venueCode);
        verify(venueRepository).findByCode(venueCode);
    }

    @Test
    @DisplayName("TEST-VENUE-002: Buscar venue por codigo inexistente lanza ResourceNotFoundException")
    void findByCode_NonExistingCode_ThrowsResourceNotFoundException() {
        String venueCode = "INVALID-VENUE";
        when(venueRepository.findByCode(venueCode)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.findByCode(venueCode))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue not found with code");

        verify(venueRepository).findByCode(venueCode);
    }

    @Test
    @DisplayName("TEST-VENUE-003: Listar venues activos retorna lista filtrada")
    void findAllActive_ReturnsActiveVenues() {
        Venue venue = new Venue();
        venue.setActive(true);

        VenueResponse response = new VenueResponse(1L, "VEN-SMR-01", "Marina Convention Center", "Santa Marta", 500, true);

        when(venueRepository.findByActiveTrue()).thenReturn(List.of(venue));
        when(venueMapper.toResponse(venue)).thenReturn(response);

        List<VenueResponse> result = venueService.findActiveVenues();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).active()).isTrue();
        verify(venueRepository).findByActiveTrue();
    }
}