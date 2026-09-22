package com.uade.marketplace.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Valida el JWT que llega en {@code Authorization: Bearer <token>} y, si es
 * valido, deja autenticado al usuario en el {@code SecurityContext} durante esa
 * request.
 *
 * <p>
 * El filtro nunca responde un error: si no hay token, esta vencido, la firma no
 * verifica o el usuario ya no existe, la request sigue como anonima y es la
 * cadena de Spring Security la que responde 401 o 403 segun la ruta.
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extraerToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<String> mail = jwtService.mailDelToken(token);
            if (mail.isPresent()) {
                autenticar(request, mail.get());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIJO_BEARER)) {
            return null;
        }
        String token = header.substring(PREFIJO_BEARER.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private void autenticar(HttpServletRequest request, String mail) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(mail);
            UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(userDetails,
                    null, userDetails.getAuthorities());
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        } catch (UsernameNotFoundException e) {
            // Token firmado por nosotros pero el usuario ya no esta en la base:
            // se lo trata como no autenticado.
            SecurityContextHolder.clearContext();
        }
    }
}
