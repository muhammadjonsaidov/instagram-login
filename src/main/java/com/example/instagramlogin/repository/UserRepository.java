package com.example.instagramlogin.repository;

import com.example.instagramlogin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByInstagramId(String id);
}
