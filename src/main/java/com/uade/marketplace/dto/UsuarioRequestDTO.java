package com.uade.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {
    private String nombreUsuario;
    private String mail;
    private String contrasena;
    private String nombre;
    private String apellido;
}