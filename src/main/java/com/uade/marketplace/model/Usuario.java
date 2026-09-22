package com.uade.marketplace.model;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreUsuario;
    private String mail;
    private String contrasena;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    // Rol con el que se arman las autoridades de Spring Security al validar el
    // JWT. El registro siempre crea usuarios con rol USUARIO; ADMIN se asigna
    // desde la base de datos.
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Rol rol = Rol.USUARIO;

    // Red de seguridad para los usuarios creados con el constructor completo
    // (tests, datos de prueba) que no indican rol.
    @PrePersist
    void asignarRolPorDefecto() {
        if (rol == null) {
            rol = Rol.USUARIO;
        }
    }
}