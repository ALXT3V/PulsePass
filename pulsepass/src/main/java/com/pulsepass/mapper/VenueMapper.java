package com.pulsepass.mapper;

import org.mapstruct.Mapper;

import com.pulsepass.domain.Venue;
import com.pulsepass.dto.response.VenueResponse;

@Mapper(componentModel = "spring")
public interface VenueMapper {
    VenueResponse toResponse(Venue venue);
    
}
