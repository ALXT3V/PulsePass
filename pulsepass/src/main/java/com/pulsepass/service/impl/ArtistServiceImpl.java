package com.pulsepass.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.mapper.ArtistMapper;
import com.pulsepass.exception.ResourceNotFoundException;

import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.service.ArtistService;

@Service
@Transactional(readOnly = true) 
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;

    public ArtistServiceImpl(ArtistRepository artistRepository, ArtistMapper artistMapper){
        this.artistRepository = artistRepository;
        this.artistMapper = artistMapper;

    }

    @Override 
    public ArtistResponse findById(Long id){
        return artistRepository.findById(id)
            .map(artistMapper :: toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found with id: "+ id));

    }

    @Override 
    public ArtistResponse findByStageName(String stageName){
        return artistRepository.findByStageName(stageName)
            .map(artistMapper :: toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found with stage name: "+ stageName));
    }
    

    @Override 
    public List<ArtistResponse> findActiveArtists(){
        return artistRepository.findAll()
            .stream()
            .filter(artist -> Boolean.TRUE.equals(artist.getActive()))
            .map(artistMapper :: toResponse)
            .toList();
    }   
}
