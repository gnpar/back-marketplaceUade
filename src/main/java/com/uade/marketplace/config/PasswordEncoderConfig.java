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
 * Se declara en una clase aparte (y no se trae el starter de seguridad
 * completo) para no activar la cadena de filtros de Spring Security por
 * defecto, que dejaria la tarea de Login/JWT y roles a cargo de otro integrante
 * del equipo.
 * </p>
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}