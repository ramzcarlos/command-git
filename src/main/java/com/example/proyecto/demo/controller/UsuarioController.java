package com.example.proyecto.demo.controller;

import com.example.proyecto.demo.Entity.Usuario;
import com.example.proyecto.demo.Repository.UsuarioRepository;
import com.example.proyecto.demo.dto.ProfileRequest;
import com.example.proyecto.demo.dto.UsuarioUpdateRequest;
import com.example.proyecto.demo.Service.UsuarioService; // servicio que implementa completarPerfil(...)
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepo;
    private final UsuarioService service;

    @GetMapping("/me")
    public Usuario me(Authentication auth) {
        Long authUserId = (Long) auth.getPrincipal();
        return usuarioRepo.findByAuthUser_Id(authUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PatchMapping("/me")
    @Transactional
    public Usuario updateMe(@RequestBody UsuarioUpdateRequest req, Authentication auth) {
        Long authUserId = (Long) auth.getPrincipal();
        Usuario u = usuarioRepo.findByAuthUser_Id(authUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (req.nombre() != null) u.setNombre(req.nombre());
        if (req.apellidoPaterno() != null) u.setApellidoPaterno(req.apellidoPaterno());
        if (req.apellidoMaterno() != null) u.setApellidoMaterno(req.apellidoMaterno());
        if (req.telefono() != null) u.setTelefono(req.telefono());
        if (req.lugarNacimiento() != null) u.setLugarNacimiento(req.lugarNacimiento());
        if (req.fechaNacimiento() != null) u.setFechaNacimiento(req.fechaNacimiento());
        if (req.semblanza() != null) u.setSemblanza(req.semblanza());

        return usuarioRepo.save(u); // asegura persistencia inmediata
    }

    @PostMapping("/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completarPerfil(@AuthenticationPrincipal(expression = "principal") Long authUserId,
                                @Valid @RequestBody ProfileRequest req) {
        service.completarPerfil(authUserId, req);
    }


}

