package com.uade.marketplace.exception;

import org.springframework.http.HttpStatus;

public class CategoriaException extends RuntimeException {

    private final HttpStatus status;

    private CategoriaException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public static CategoriaException noEncontrada(Long id) {
        return new CategoriaException("Categoría con ID " + id + " no encontrada", HttpStatus.NOT_FOUND);
    }

    public static CategoriaException nombreYaExiste(String nombre) {
        return new CategoriaException("Ya existe una categoría con el nombre: " + nombre, HttpStatus.CONFLICT);
    }

    public HttpStatus getStatus() {
        return status;
    }
}