package com.example.tandem_api.repository;

import com.example.tandem_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>{
    boolean existsByEmail(String email);
}