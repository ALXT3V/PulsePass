package com.pulsepass.service;

import com.pulsepass.domain.Artist;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.ArtistMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.service.impl.ArtistServiceImpl;
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
class ArtistServiceImplTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtistMapper artistMapper;

    @InjectMocks
    private ArtistServiceImpl artistService;

    @Test
    @DisplayName("TEST-ARTIST-001: Buscar artista por ID existente retorna DTO")
    void findById_ExistingId_ReturnsArtistResponse() {
        Long artistId = 1L;
        Artist artist = new Artist();
        ArtistResponse expectedResponse = new ArtistResponse(artistId, "Shakira", "Pop/Latin", true);

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(expectedResponse);

        ArtistResponse result = artistService.findById(artistId);

        assertThat(result).isNotNull();
        assertThat(result.stageName()).isEqualTo("Shakira");
        verify(artistRepository).findById(artistId);
    }

    @Test
    @DisplayName("TEST-ARTIST-002: Buscar artista por ID inexistente lanza ResourceNotFoundException")
    void findById_NonExistingId_ThrowsResourceNotFoundException() {
        Long artistId = 99L;
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.findById(artistId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artist not found with id");

        verify(artistRepository).findById(artistId);
    }

    @Test
    @DisplayName("TEST-ARTIST-003: Buscar artista por nombre artistico existente retorna DTO")
    void findByStageName_ExistingName_ReturnsArtistResponse() {
        String stageName = "Shakira";
        Artist artist = new Artist();
        ArtistResponse expectedResponse = new ArtistResponse(1L, stageName, "Pop/Latin", true);

        when(artistRepository.findByStageName(stageName)).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(expectedResponse);

        ArtistResponse result = artistService.findByStageName(stageName);

        assertThat(result).isNotNull();
        assertThat(result.stageName()).isEqualTo(stageName);
        verify(artistRepository).findByStageName(stageName);
    }

    @Test
    @DisplayName("TEST-ARTIST-004: Buscar artista por nombre artistico inexistente lanza ResourceNotFoundException")
    void findByStageName_NonExistingName_ThrowsResourceNotFoundException() {
        String stageName = "UNKNOWN";
        when(artistRepository.findByStageName(stageName)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.findByStageName(stageName))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artist not found with stage name");

        verify(artistRepository).findByStageName(stageName);
    }

    @Test
    @DisplayName("TEST-ARTIST-005: Listar artistas activos retorna lista filtrada")
    void findActiveArtist_ReturnsActiveArtists() {
        Artist artist = new Artist();
        ArtistResponse response = new ArtistResponse(1L, "Shakira", "Pop/Latin", true);

        when(artistRepository.findAll()).thenReturn(List.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(response);

        List<ArtistResponse> result = artistService.findActiveArtist();

        assertThat(result).isNotNull();
        verify(artistRepository).findAll();
    }
}