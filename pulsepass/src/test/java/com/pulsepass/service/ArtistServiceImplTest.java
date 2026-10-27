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
    @DisplayName("TEST-ARTIST-001: Buscar artista por ID existente retorna ArtistResponse (FR-SVC-009)")
    void findById_ExistingId_ReturnsArtistResponse() {
        Long artistId = 1L;
        Artist artist = new Artist();
        ArtistResponse expectedResponse = mock(ArtistResponse.class);

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(expectedResponse);

        ArtistResponse result = artistService.findById(artistId);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
        verify(artistRepository).findById(artistId);
    }

    @Test
    @DisplayName("TEST-ARTIST-002: BR-ARTIST-001 - Artista por ID inexistente lanza ResourceNotFoundException")
    void findById_NonExistingId_ThrowsResourceNotFoundException() {
        Long artistId = 99L;
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.findById(artistId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artist not found with id: " + artistId);

        verify(artistRepository).findById(artistId);
    }

    @Test
    @DisplayName("TEST-ARTIST-003: Buscar artista por stageName existente retorna ArtistResponse")
    void findByStageName_ExistingStageName_ReturnsArtistResponse() {
        String stageName = "Solar Beat";
        Artist artist = new Artist();
        ArtistResponse expectedResponse = mock(ArtistResponse.class);

        when(artistRepository.findByStageName(stageName)).thenReturn(Optional.of(artist));
        when(artistMapper.toResponse(artist)).thenReturn(expectedResponse);

        ArtistResponse result = artistService.findByStageName(stageName);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
        verify(artistRepository).findByStageName(stageName);
    }

    @Test
    @DisplayName("TEST-ARTIST-004: BR-ARTIST-001 - stageName inexistente lanza ResourceNotFoundException")
    void findByStageName_NonExistingStageName_ThrowsResourceNotFoundException() {
        String stageName = "Unknown Band";
        when(artistRepository.findByStageName(stageName)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> artistService.findByStageName(stageName))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Artist not found with stage name: " + stageName);

        verify(artistRepository).findByStageName(stageName);
    }

     @Test
    @DisplayName("TEST-ARTIST-005: BR-ARTIST-002 - findActiveArtists() retorna solo artistas activos")
    void findActiveArtists_ReturnsActiveArtistsOnly() {
        Artist activeArtist = new Artist();
        activeArtist.setStageName("Solar Beat");
        activeArtist.setActive(true);

        Artist inactiveArtist = new Artist();
        inactiveArtist.setStageName("Old Band");
        inactiveArtist.setActive(false);

        ArtistResponse response = mock(ArtistResponse.class);

        when(artistRepository.findAll()).thenReturn(List.of(activeArtist, inactiveArtist));
        when(artistMapper.toResponse(activeArtist)).thenReturn(response);

        List<ArtistResponse> result = artistService.findActiveArtists();

        assertThat(result).containsExactly(response);
        verify(artistMapper, never()).toResponse(inactiveArtist);
    }
}