package com.uade.marketplace.exception;

import org.springframework.http.HttpStatus;

public class ProductoException extends RuntimeException {

    private final HttpStatus status;

    private ProductoException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public static ProductoException noEncontrado(Long id) {
        return new ProductoException(
                "Producto con ID " + id + " no encontrado", HttpStatus.NOT_FOUND);
    }

    public HttpStatus getStatus() {
        return status;
    }
}