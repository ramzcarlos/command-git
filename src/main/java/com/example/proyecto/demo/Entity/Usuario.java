package com.example.proyecto.demo.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import  com.example.proyecto.demo.Entity.Direccion;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// se crea entitad para realizar el login con email y contraseña


@Entity

@Table(name = "usuarios",
        indexes = {
                @Index(name = "idx_usuarios_email", columnList = "email", unique = true),
                @Index(name = "idx_usuarios_curp", columnList = "curp")
        })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincremental
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "nombre", length = 120, nullable = false)
    private String nombre;

    @NotBlank
    @Column(name = "apellido_paterno", length = 120, nullable = false)
    private String apellidoPaterno;

    @NotBlank
    @Column(name = "apellido_materno", length = 120, nullable = false)
    private String apellidoMaterno;

    @Email
    @NotBlank
    @Column(name = "email", length = 180, nullable = false, unique = true)
    private String email;

    @Column(name = "semblanza", length = 2000)
    private String semblanza;

    @OneToOne
    @JsonManagedReference
    @JoinColumn(name = "auth_user_id", unique = true)
    private AuthUser authUser;


    // Mejor como String (e.g., +52XXXXXXXXXX)
    @Size(min = 7, max = 20)
    @Column(name = "telefono", length = 20)
    private String telefono;

    // CURP mexicano: 18 chars. (regex simplificada)
    @Pattern(regexp = "^[A-Z0-9]{18}$", message = "CURP inválida")
    @Column(name = "curp", length = 18)
    private String curp;

    @Column(name = "lugar_nacimiento", length = 150)
    private String lugarNacimiento;

    @Past
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    // RFC puede ser 12 (moral) o 13 (física). (regex simplificada)
    @Pattern(regexp = "^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{2,3}$", message = "RFC inválido")
    @Column(name = "rfc", length = 13)
    private String rfc;

    @Column(name = "nacionalidad", length = 80)
    private String nacionalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", length = 20)
    private EstadoCivil estadoCivil;

    // Colección simple de URLs o handles
    @ElementCollection
    @CollectionTable(name = "usuario_redes_sociales",
            joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "red_social", length = 200)
    private List<String> redesSociales = new ArrayList<>();

    // Relación 1–1 con Dirección
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "direccion_id")
    private Direccion direccion;

    // Evidencias/documentos 1–N
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Documento> documentos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private java.time.Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private java.time.Instant actualizadoEn;

    public enum EstadoCivil { SOLTERO, CASADO, UNION_LIBRE, DIVORCIADO, VIUDO }
}
