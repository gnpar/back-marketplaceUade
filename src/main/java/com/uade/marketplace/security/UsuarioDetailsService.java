package com.uade.marketplace.security;

import com.uade.marketplace.model.Rol;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Puente entre la entidad {@link Usuario} y el {@link UserDetails} que entiende
 * Spring Security.
 *
 * <p>
 * El "username" de Spring Security es el mail, que es tambien el subject del
 * JWT: por eso {@code Principal.getName()} en los controllers sigue siendo el
 * mail del usuario autenticado. Las autoridades salen del rol guardado en la
 * base de datos, no del token.
 * </p>
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByMail(mail)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con el mail: " + mail));

        Rol rol = usuario.getRol() != null ? usuario.getRol() : Rol.USUARIO;

        return User.withUsername(usuario.getMail()).password(usuario.getContrasena())
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol.name())).build();
    }
}
