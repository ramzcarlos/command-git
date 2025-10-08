package com.example.proyecto.demo.Entity;

import com.example.proyecto.demo.Entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "documentos",
        indexes = @Index(name = "idx_documentos_usuario", columnList = "usuario_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 30, nullable = false)
    private TipoDocumento tipo;

    @NotBlank
    @Column(name = "nombre_archivo", length = 255, nullable = false)
    private String nombreArchivo;

    @NotBlank
    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;

    @Positive
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    // Opción A: almacenar ruta/URL (recomendado para archivos pesados)
    @NotBlank
    @Column(name = "ubicacion", length = 500, nullable = false)
    private String ubicacion; // p.ej. /files/curp/123.pdf o URL S3



    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public enum TipoDocumento {
        CURP,
        ACTA_NACIMIENTO,
        INE_FRENTE,
        INE_REVERSO,
        OTRO
    }
}
