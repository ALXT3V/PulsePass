package com.pulsepass.service;

import java.util.List;

import com.pulsepass.dto.response.VenueResponse;

public interface VenueService {
    VenueResponse findByCode(String code);
    List<VenueResponse> findActiveVenues();
    
}
