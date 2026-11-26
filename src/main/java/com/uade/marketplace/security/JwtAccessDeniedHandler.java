package com.uade.marketplace.security;

import com.uade.marketplace.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Respuesta cuando el usuario esta autenticado pero su rol no alcanza para la
 * ruta pedida, o cuando falta el token CSRF: 403 con el formato de error de la
 * API.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String mensaje = accessDeniedException instanceof CsrfException
                ? "Falta el token CSRF o no es valido. Recarga la pagina y proba de nuevo"
                : "No tenes permisos para acceder a este recurso";
        objectMapper.writeValue(response.getWriter(), new ErrorResponseDTO(HttpStatus.FORBIDDEN.value(), mensaje));
    }
}
