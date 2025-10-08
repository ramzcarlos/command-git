package com.example.proyecto.demo.Service;

import com.example.proyecto.demo.Entity.AuthUser;
import com.example.proyecto.demo.Entity.Usuario;
import com.example.proyecto.demo.Repository.AuthUserRepository;
import com.example.proyecto.demo.Repository.UsuarioRepository;
import com.example.proyecto.demo.dto.AuthResponse;
import com.example.proyecto.demo.dto.LoginRequest;
import com.example.proyecto.demo.dto.RegisterRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepo;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder; // BCrypt
    //private final SecretKey key;            // inyectado

    private final javax.crypto.SecretKey key;


    @Transactional
    public AuthResponse register(RegisterRequest req) {  // <- devuelve AuthResponse
        if (authUserRepo.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
        }

        AuthUser au = AuthUser.builder()
                .email(req.email())
                .username(req.username() != null ? req.username() : req.email())
                .passwordHash(encoder.encode(req.password()))
                .enabled(true)
                .roles(new HashSet<>(List.of("ROLE_USER")))
                .build();
        authUserRepo.save(au);

//        Usuario perfil = Usuario.builder()
//                .email(req.email())
//                .authUser(au)
//                .build();
//        usuarioRepo.save(perfil);

        String token = Jwts.builder()
                .subject(String.valueOf(au.getId()))
                .claim("username", au.getUsername())
                .claim("roles", au.getRoles())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000)) // 1h
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new AuthResponse(au.getId(), au.getUsername(), token);
    }

    public String login(LoginRequest req) {
        AuthUser au = authUserRepo.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales"));
        if (!encoder.matches(req.password(), au.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales");
        }
        // Si ya tienes JwtUtil y ahí generas el token, úsalo:
        // return jwtUtil.generateToken(au.getId(), au.getUsername(), au.getRoles());
        String token = Jwts.builder()
                .subject(String.valueOf(au.getId()))
                .claim("username", au.getUsername())
                .claim("roles", au.getRoles())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return token;
    }
}
