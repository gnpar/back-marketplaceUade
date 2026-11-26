package com.uade.marketplace.config;

import com.uade.marketplace.security.CookieSesionService;
import com.uade.marketplace.security.CsrfCookieFilter;
import com.uade.marketplace.security.JwtAccessDeniedHandler;
import com.uade.marketplace.security.JwtAuthenticationEntryPoint;
import com.uade.marketplace.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Configuracion de Spring Security: API stateless autenticada por JWT.
 *
 * <p>
 * No hay sesion en el servidor ni login por formulario: cada request se
 * autentica con un JWT que valida {@link JwtAuthenticationFilter}. El JWT llega
 * en el header {@code Authorization: Bearer <token>} (tests, Postman) o en la
 * cookie HttpOnly {@code marketplace_token} que pone el login (el front).
 * </p>
 *
 * <p>
 * <b>CSRF.</b> Como el navegador manda la cookie solo, otro sitio podria
 * intentar que el usuario dispare una request sin querer. Defensas:
 * </p>
 * <ol>
 * <li>La cookie de sesion es {@code SameSite=Strict} (ver
 * {@link CookieSesionService}).</li>
 * <li>Patron "double submit cookie": el back manda un token aleatorio en la
 * cookie {@code XSRF-TOKEN} (legible por JS) y en cada POST/PUT/DELETE/PATCH
 * que se autentica con la cookie de sesion exige el mismo valor en el header
 * {@code X-XSRF-TOKEN}. Un sitio ajeno no puede leer esa cookie, asi que no
 * puede armar el header: la request se rechaza con 403.</li>
 * </ol>
 * <p>
 * Las requests que se autentican con el header {@code Authorization} no
 * necesitan el token CSRF: el navegador nunca agrega ese header por su cuenta.
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
    private final CookieSesionService cookieSesionService;

    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint, JwtAccessDeniedHandler accessDeniedHandler,
            CookieSesionService cookieSesionService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.cookieSesionService = cookieSesionService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Cookie XSRF-TOKEN legible por JS (no es secreta para el propio sitio: su
        // valor solo sirve si ademas viaja en el header X-XSRF-TOKEN).
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookieCustomizer(cookie -> cookie.sameSite("Strict").path("/"));

        http.csrf(csrf -> csrf.csrfTokenRepository(csrfRepository)
                // El front manda en el header el valor tal cual lo lee de la cookie.
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .requireCsrfProtectionMatcher(this::requiereProteccionCsrf))
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // La consola de H2 se dibuja dentro de un frame del mismo origen.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // Publico: registro, login, logout y catalogo de solo lectura.
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/registro", "/api/usuarios/login",
                                "/api/usuarios/logout")
                        .permitAll().requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**")
                        .permitAll().requestMatchers("/error", "/actuator/**", "/h2-console/**").permitAll()
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

    /**
     * Pide el token CSRF solo cuando la request modifica datos y se autentica con
     * la cookie de sesion: es el unico caso en que el navegador manda la credencial
     * por su cuenta.
     */
    private boolean requiereProteccionCsrf(HttpServletRequest request) {
        if (METODOS_SEGUROS.contains(request.getMethod())) {
            return false;
        }
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            return false;
        }
        return cookieSesionService.tieneCookieSesion(request);
    }
}
