// src/main/java/com/example/proyecto/demo/dto/ProfileRequest.java
package com.example.proyecto.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileRequest(
        @NotBlank String nombre,
        @NotBlank String apellidoPaterno,
        @NotBlank String apellidoMaterno
) {}
