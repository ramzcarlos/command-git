package com.example.proyecto.demo.controller;

import com.example.proyecto.demo.Service.AuthService;
import com.example.proyecto.demo.Service.JwtService;
import com.example.proyecto.demo.dto.JwtResponse;
import com.example.proyecto.demo.dto.LoginRequest;
import com.example.proyecto.demo.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest req) {
        return new JwtResponse(authService.login(req));
    }

    @PostMapping("/login-admin")
    public String loginAdmin() {
        return jwtService.generateToken(1L, "admin", List.of("ROLE_ADMIN"));
    }

    @PostMapping("/login-user")
    public String loginUser() {
        return jwtService.generateToken(2L, "usuario", List.of("ROLE_USER"));
    }
}


