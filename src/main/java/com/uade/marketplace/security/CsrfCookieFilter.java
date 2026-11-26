package com.uade.marketplace.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Fuerza a Spring Security a generar el token CSRF en cada request para que la
 * cookie {@code XSRF-TOKEN} llegue al navegador desde la primera respuesta (por
 * ejemplo, {@code GET /api/usuarios/me} al abrir la app).
 *
 * <p>
 * Spring carga el token de forma diferida: si nadie lo lee, no escribe la
 * cookie. Este filtro lo lee y listo. No se registra como {@code @Component}
 * (Spring Boot lo agregaria tambien fuera de la cadena de seguridad): lo
 * instancia {@code SecurityConfig}.
 * </p>
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Leer el valor dispara que el repositorio guarde la cookie.
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
