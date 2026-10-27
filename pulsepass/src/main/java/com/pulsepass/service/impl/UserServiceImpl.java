package com.pulsepass.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.UserMapper;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.UserService;

@Service 
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override 
    @Transactional 
    public UserResponse register(RegisterUserRequest request){
        if(request.birthDate() != null && request.birthDate().isAfter(LocalDate.now())){
            throw new BusinessRuleException("Birth date cannot be in the future");

        }
        if(userRepository.findByUsername(request.username()).isPresent()){
            throw new DuplicateResourceException("User already exists with username: "+ request.username());
        }
        if (userRepository.findByEmail(request.email()).isPresent()){
            throw new DuplicateResourceException("User already exists with email: "+ request.email());
        }
        
        User user = userMapper.toEntity(request);
        user.setActive(true);

        UserProfile profile = new UserProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        profile.setCity(request.city());
        profile.setBirthDate(request.birthDate());
        profile.setUser(user);

        user.setProfile(profile);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);

    }

    @Override 
    public  UserResponse findByEmail(String email){
        return userRepository.findByEmail(email)
            .map(userMapper :: toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: "+ email));
            
    }

    @Override 
    public UserResponse findByUsername(String username){
        return userRepository.findByUsername(username)
            .map(userMapper :: toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: "+username));
            
    }
}
