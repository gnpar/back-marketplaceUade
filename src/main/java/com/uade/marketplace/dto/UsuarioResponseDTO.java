package com.uade.marketplace.dto;

import com.uade.marketplace.model.Sexo;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String nombreUsuario;
    private String mail;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private Sexo sexo;
}