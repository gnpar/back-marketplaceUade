package com.uade.marketplace.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Emision y validacion de los JWT.
 *
 * <p>
 * Contrato del token (acordado con la emision del login): firmado con HS256 y
 * la clave {@code jwt.secret}, {@code sub} = mail del usuario, {@code iat} =
 * emision y {@code exp} = emision + {@code jwt.expiration} ms. El rol NO viaja
 * en el token: se lee de la base de datos al validarlo, asi un cambio de rol
 * tiene efecto sin esperar a que venza el token.
 * </p>
 */
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generarToken(String mail) {
        return Jwts.builder().setSubject(mail).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    /**
     * Devuelve el mail (subject) del token si la firma es valida y el token no esta
     * vencido; {@link Optional#empty()} en cualquier otro caso (firma invalida,
     * token adulterado, vencido o mal formado).
     */
    public Optional<String> mailDelToken(String token) {
        try {
            return Optional.ofNullable(parsearClaims(token).getSubject()).filter(mail -> !mail.isBlank());
        } catch (JwtException | IllegalArgumentException e) {
            // Token invalido: no se autentica al usuario y la request sigue como
            // anonima; quien decide el 401 es la cadena de Spring Security.
            return Optional.empty();
        }
    }

    /** Valida firma y vencimiento. */
    public boolean esValido(String token) {
        return mailDelToken(token).isPresent();
    }

    // parseClaimsJws verifica la firma y lanza ExpiredJwtException si venció.
    private Claims parsearClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }
}
