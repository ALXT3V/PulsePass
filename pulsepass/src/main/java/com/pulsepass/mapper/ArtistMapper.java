package com.pulsepass.mapper;

import org.mapstruct.Mapper;

import com.pulsepass.domain.Artist;
import com.pulsepass.dto.response.ArtistResponse;

@Mapper(componentModel="spring")
public interface ArtistMapper {
    ArtistResponse toResponse(Artist artist);
    
}
