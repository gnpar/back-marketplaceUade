package com.uade.marketplace.exception;

import org.springframework.http.HttpStatus;

public class CarritoException extends RuntimeException {

    private final HttpStatus status;

    private CarritoException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public static CarritoException noEncontrado(Long id) {
        return new CarritoException("Item del carrito con ID " + id + " no encontrado", HttpStatus.NOT_FOUND);
    }

    public static CarritoException cantidadInvalida() {
        return new CarritoException("La cantidad debe ser mayor a 0", HttpStatus.BAD_REQUEST);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static CarritoException carritoVacio(Long usuarioId) {
        return new CarritoException("El carrito del usuario " + usuarioId + " está vacío", HttpStatus.BAD_REQUEST);
    }

}