package com.uade.marketplace.repository;

import com.uade.marketplace.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByMail(String mail);

    boolean existsByMail(String mail);

    boolean existsByNombreUsuario(String nombreUsuario);
}