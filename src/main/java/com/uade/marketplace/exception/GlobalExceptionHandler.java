package com.uade.marketplace.exception;

import com.uade.marketplace.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Excepciones de Usuario (noEncontrado, mailYaRegistrado, etc.)
    @ExceptionHandler(UsuarioException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsuarioException(UsuarioException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getStatus().value(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Excepciones de Producto
    @ExceptionHandler(ProductoException.class)
    public ResponseEntity<ErrorResponseDTO> handleProductoException(ProductoException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getStatus().value(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Excepciones de validación estándar de Java (por si algún servicio la usa en
    // vez de una excepción propia)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Catch-all: cualquier otra excepción no contemplada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}