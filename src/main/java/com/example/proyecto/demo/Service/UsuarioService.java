package com.example.proyecto.demo.Service;

import com.example.proyecto.demo.Entity.AuthUser;
import com.example.proyecto.demo.Entity.Usuario;
import com.example.proyecto.demo.Repository.AuthUserRepository;
import com.example.proyecto.demo.Repository.UsuarioRepository;
import com.example.proyecto.demo.dto.ProfileRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final AuthUserRepository authUserRepo;


    @Transactional
    public void completarPerfil(Long authUserId, ProfileRequest req) {
        // 1) Obtén el AuthUser (dueño del perfil)
        AuthUser au = authUserRepo.findById(authUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AuthUser no encontrado"));

        // 2) Busca si ya hay Usuario; si no, crea uno nuevo asociado a AuthUser
        Usuario u = usuarioRepo.findByAuthUser_Id(authUserId)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setAuthUser(au);
                    // Mantén consistente el email con el de AuthUser
                    nuevo.setEmail(au.getEmail());
                    return nuevo;
                });

        // 3) Asigna los campos obligatorios (validados por @NotBlank en la entidad)
        u.setNombre(req.nombre());
        u.setApellidoPaterno(req.apellidoPaterno());
        u.setApellidoMaterno(req.apellidoMaterno());

        // 4) Guarda/actualiza
        usuarioRepo.save(u);
    }
}
