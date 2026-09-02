package com.uade.marketplace.service;

import com.uade.marketplace.dto.LoginRequestDTO;
import com.uade.marketplace.dto.UsuarioRequestDTO;
import com.uade.marketplace.dto.UsuarioResponseDTO;
import com.uade.marketplace.exception.UsuarioException;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponseDTO> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioResponseDTO> dtos = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            dtos.add(convertirADTO(usuario));
        }
        return dtos;
    }

    public UsuarioResponseDTO getUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> UsuarioException.noEncontrado(id));
        return convertirADTO(usuario);
    }

    // Registro
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO usuarioDTO) {
        if (usuarioRepository.existsByMail(usuarioDTO.getMail())) {
            throw UsuarioException.mailYaRegistrado(usuarioDTO.getMail());
        }
        if (usuarioRepository.existsByNombreUsuario(usuarioDTO.getNombreUsuario())) {
            throw UsuarioException.nombreUsuarioYaRegistrado(usuarioDTO.getNombreUsuario());
        }
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(usuarioDTO.getNombreUsuario());
        usuario.setMail(usuarioDTO.getMail());
        usuario.setContrasena(usuarioDTO.getContrasena());
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        Usuario guardado = usuarioRepository.save(usuario);
        return convertirADTO(guardado);
    }

    // Login: identifica por mail + contraseña
    public UsuarioResponseDTO login(LoginRequestDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByMail(loginDTO.getMail()).orElse(null);
        if (usuario == null || !usuario.getContrasena().equals(loginDTO.getContrasena())) {
            throw UsuarioException.credencialesIncorrectas();
        }
        return convertirADTO(usuario);
    }

    private UsuarioResponseDTO convertirADTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombreUsuario(usuario.getNombreUsuario());
        dto.setMail(usuario.getMail());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        return dto;
    }
}