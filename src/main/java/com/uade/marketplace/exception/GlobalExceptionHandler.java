package com.uade.marketplace.exception;

import com.uade.marketplace.dto.ErrorResponseDTO;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Errores de validacion de los DTO (@Valid / @Validated): 400 Bad Request
    // con el detalle de cada campo que no cumplio una restriccion.
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining("; "));
        ErrorResponseDTO error = new ErrorResponseDTO(HttpStatus.BAD_REQUEST.value(), mensaje);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(error);
    }

    // Conserva los codigos y headers de Spring (404, 405, 415, etc.), pero usa
    // siempre el mismo DTO. No expone detalles del parser ni valores recibidos.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String mensaje;
        if (status.is5xxServerError()) {
            log.error("Error inesperado al procesar la solicitud", ex);
            mensaje = "Ocurrio un error interno. Intente nuevamente mas tarde";
        } else if (ex instanceof HttpMessageNotReadableException) {
            mensaje = "El cuerpo de la solicitud es obligatorio y debe contener un JSON valido con los tipos correctos";
        } else if (ex instanceof TypeMismatchException) {
            mensaje = "Un parametro de la solicitud tiene un tipo o formato incorrecto";
        } else if (ex instanceof HandlerMethodValidationException validationException) {
            mensaje = validationException.getAllErrors().stream().map(error -> error.getDefaultMessage()).distinct()
                    .sorted().collect(Collectors.joining("; "));
        } else {
            mensaje = switch (status.value()) {
                case 400 -> "La solicitud contiene parametros invalidos";
                case 404 -> "El recurso solicitado no existe";
                case 405 -> "El metodo HTTP no esta permitido para este recurso";
                case 406 -> "El formato de respuesta solicitado no esta disponible";
                case 413 -> "El contenido de la solicitud supera el tamanio permitido";
                case 415 -> "El tipo de contenido de la solicitud no esta soportado";
                default -> "No se pudo procesar la solicitud";
            };
        }
        return super.handleExceptionInternal(ex, new ErrorResponseDTO(status.value(), mensaje), headers, status,
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDTO(HttpStatus.CONFLICT.value(),
                "La operacion entra en conflicto con los datos existentes o sus relaciones"));
    }

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

    @ExceptionHandler(ImagenProductoException.class)
    public ResponseEntity<ErrorResponseDTO> handleImagenProductoException(ImagenProductoException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getStatus().value(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Excepciones de Carrito
    @ExceptionHandler(CarritoException.class)
    public ResponseEntity<ErrorResponseDTO> handleCarritoException(CarritoException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getStatus().value(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // Excepciones de Categoria
    @ExceptionHandler(CategoriaException.class)
    public ResponseEntity<ErrorResponseDTO> handleCategoriaException(CategoriaException ex) {
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
        log.error("Error inesperado al procesar la solicitud", ex);
        ErrorResponseDTO error = new ErrorResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrio un error interno. Intente nuevamente mas tarde");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
