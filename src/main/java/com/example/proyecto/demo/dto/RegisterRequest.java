package com.example.proyecto.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank String username,
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password
)
{}