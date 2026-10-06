package com.pulsepass.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pulsepass.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.profile WHERE u.active = true")
    List<User> findAllActiveWithProfile();

    @Query("SELECT u FROM User u JOIN u.profile p WHERE LOWER(p.city) = LOWER(:city)")
    List<User> findByProfileCity(@Param("city") String city);
}