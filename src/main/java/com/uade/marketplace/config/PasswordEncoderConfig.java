package com.uade.marketplace.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Expone el {@link PasswordEncoder} usado para hashear las contrasenas.
 *
 * <p>
 * Se usa BCrypt: cada hash incluye su propio salt aleatorio, por lo que dos
 * usuarios con la misma contrasena obtienen hashes distintos y nunca se
 * almacena la contrasena en texto plano.
 * </p>
 *
 * <p>
 * Se declara en una clase aparte de {@link SecurityConfig} para que el registro
 * y el login dependan solo del encoder y no de toda la configuracion de la
 * cadena de filtros.
 * </p>
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}