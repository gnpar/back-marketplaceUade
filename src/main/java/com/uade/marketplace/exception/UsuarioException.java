package com.uade.marketplace.exception;

import org.springframework.http.HttpStatus;

public class UsuarioException extends RuntimeException {

    private final HttpStatus status;

    private UsuarioException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public static UsuarioException noEncontrado(Long id) {
        return new UsuarioException("Usuario con ID " + id + " no encontrado", HttpStatus.NOT_FOUND);
    }

    public static UsuarioException mailYaRegistrado(String mail) {
        return new UsuarioException("Ya existe un usuario con el mail: " + mail, HttpStatus.CONFLICT);
    }

    public static UsuarioException nombreUsuarioYaRegistrado(String nombreUsuario) {
        return new UsuarioException("Ya existe un usuario con el nombre de usuario: " + nombreUsuario,
                HttpStatus.CONFLICT);
    }

    public static UsuarioException credencialesIncorrectas() {
        return new UsuarioException("Mail o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
    }

    public HttpStatus getStatus() {
        return status;
    }
}