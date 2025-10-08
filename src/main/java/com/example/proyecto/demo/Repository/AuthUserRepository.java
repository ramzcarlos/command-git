package com.example.proyecto.demo.Repository;

import com.example.proyecto.demo.Entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;   

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
