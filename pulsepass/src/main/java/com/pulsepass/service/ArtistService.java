package com.pulsepass.service;

import java.util.List;

import com.pulsepass.dto.response.ArtistResponse;

public interface ArtistService {
    ArtistResponse findById(Long id);
    ArtistResponse findByStageName(String stageName);
    List<ArtistResponse> findActiveArtist();
}
