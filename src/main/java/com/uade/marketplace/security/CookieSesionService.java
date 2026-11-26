package com.uade.marketplace.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Arma y lee la cookie que transporta el JWT de la sesion del navegador.
 *
 * <p>
 * El token NO viaja en el body del login ni se guarda en localStorage: el back
 * lo pone en una cookie con estos atributos, y el navegador la manda sola en
 * cada request a {@code /api}:
 * </p>
 * <ul>
 * <li>{@code HttpOnly}: JavaScript no puede leerla ({@code document.cookie} no
 * la ve), asi que un ataque XSS no puede robar el token.</li>
 * <li>{@code Secure}: solo viaja por HTTPS (los navegadores aceptan
 * {@code localhost} por HTTP). Se desactiva con
 * {@code AUTH_COOKIE_SECURE=false} si hace falta.</li>
 * <li>{@code SameSite=Strict}: el navegador no la manda en requests que se
 * originan en otro sitio; es la primera barrera contra CSRF (la segunda es el
 * token CSRF de {@code SecurityConfig}).</li>
 * <li>{@code Path=/api}: no se manda al pedir el HTML, JS o imagenes.</li>
 * <li>{@code Max-Age}: igual al vencimiento del JWT (24 h), asi la sesion
 * sobrevive a un F5 o a cerrar el navegador.</li>
 * </ul>
 */
@Component
public class CookieSesionService {

    public static final String NOMBRE_COOKIE = "marketplace_token";
    private static final String PATH_COOKIE = "/api";

    @Value("${jwt.expiration}")
    private long expiracionMs;

    @Value("${app.auth.cookie-secure:true}")
    private boolean cookieSegura;

    /** Cookie con el JWT recien emitido por el login. */
    public ResponseCookie crearCookieSesion(String token) {
        return base(token).maxAge(Duration.ofMillis(expiracionMs)).build();
    }

    /** Cookie vacia y vencida: le indica al navegador que borre la sesion. */
    public ResponseCookie crearCookieBorrado() {
        return base("").maxAge(Duration.ZERO).build();
    }

    /** Devuelve el JWT de la cookie de sesion, o null si la request no la trae. */
    public String leerToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (NOMBRE_COOKIE.equals(cookie.getName())) {
                String valor = cookie.getValue();
                return valor == null || valor.isBlank() ? null : valor;
            }
        }
        return null;
    }

    /** True si la request trae la cookie de sesion (la usa el filtro de CSRF). */
    public boolean tieneCookieSesion(HttpServletRequest request) {
        return leerToken(request) != null;
    }

    private ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(NOMBRE_COOKIE, valor).httpOnly(true).secure(cookieSegura).sameSite("Strict")
                .path(PATH_COOKIE);
    }
}
