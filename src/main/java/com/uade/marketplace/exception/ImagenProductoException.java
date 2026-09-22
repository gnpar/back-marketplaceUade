package com.uade.marketplace.exception;

import org.springframework.http.HttpStatus;

public class ImagenProductoException extends RuntimeException {

    private final HttpStatus status;

    private ImagenProductoException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }

    public static ImagenProductoException noEncontrada(Long id) {
        return new ImagenProductoException("Imagen con ID " + id + " no encontrada para el producto",
                HttpStatus.NOT_FOUND);
    }

    public static ImagenProductoException archivoInvalido(String mensaje) {
        return new ImagenProductoException(mensaje, HttpStatus.BAD_REQUEST);
    }

    public static ImagenProductoException limiteAlcanzado(int limite) {
        return new ImagenProductoException("El producto no puede tener más de " + limite + " imágenes",
                HttpStatus.BAD_REQUEST);
    }

    public static ImagenProductoException errorDeLectura() {
        return new ImagenProductoException("No se pudo leer el archivo de imagen", HttpStatus.BAD_REQUEST);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
