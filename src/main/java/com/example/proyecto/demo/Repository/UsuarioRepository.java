package com.example.proyecto.demo.Repository;

import com.example.proyecto.demo.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca por el id del AuthUser asociado
    Optional<Usuario> findByAuthUser_Id(Long authUserId);

    // (opcional) Busca por el email del AuthUser
    Optional<Usuario> findByAuthUser_Email(String email);
}

