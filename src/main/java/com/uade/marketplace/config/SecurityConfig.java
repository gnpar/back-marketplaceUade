package com.uade.marketplace.config;

import com.uade.marketplace.security.JwtAccessDeniedHandler;
import com.uade.marketplace.security.JwtAuthenticationEntryPoint;
import com.uade.marketplace.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracion de Spring Security: API stateless autenticada por JWT.
 *
 * <p>
 * No hay sesion ni login por formulario: cada request se autentica con el
 * header {@code Authorization: Bearer <token>} que valida
 * {@link JwtAuthenticationFilter}. Por eso tambien se desactiva CSRF, que
 * protege flujos con cookies de sesion.
 * </p>
 *
 * <p>
 * Los permisos por ruta se definen abajo en {@link #securityFilterChain} y
 * estan documentados en el README.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint, JwtAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // La consola de H2 se dibuja dentro de un frame del mismo origen.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // Publico: registro, login y catalogo de solo lectura.
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/registro", "/api/usuarios/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**").permitAll()
                        .requestMatchers("/error", "/actuator/**", "/h2-console/**").permitAll()
                        // Solo ADMIN: gestion del catalogo de categorias y listado
                        // completo de usuarios.
                        .requestMatchers("/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole("ADMIN")
                        // Todo lo demas (carrito, alta/edicion de productos, datos de
                        // usuario) exige un JWT valido.
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
