package com.uade.marketplace.service;

import com.uade.marketplace.dto.LoginRequestDTO;
import com.uade.marketplace.dto.UsuarioRequestDTO;
import com.uade.marketplace.dto.UsuarioResponseDTO;
import com.uade.marketplace.exception.UsuarioException;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> UsuarioException.noEncontrado(id));
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
        validarFechaNacimientoYSexo(usuarioDTO);

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(usuarioDTO.getNombreUsuario());
        usuario.setMail(usuarioDTO.getMail());
        usuario.setContrasena(usuarioDTO.getContrasena());
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setFechaNacimiento(usuarioDTO.getFechaNacimiento());
        usuario.setSexo(usuarioDTO.getSexo());
        Usuario guardado = usuarioRepository.save(usuario);
        return convertirADTO(guardado);
    }

    private void validarFechaNacimientoYSexo(UsuarioRequestDTO usuarioDTO) {
        if (usuarioDTO.getFechaNacimiento() == null) {
            throw UsuarioException.datosInvalidos("La fecha de nacimiento es obligatoria");
        }
        if (usuarioDTO.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw UsuarioException.datosInvalidos("La fecha de nacimiento no puede ser una fecha futura");
        }
        if (usuarioDTO.getSexo() == null) {
            throw UsuarioException.datosInvalidos("El sexo es obligatorio");
        }
    }

    // Login: identifica por mail + contraseña
    public UsuarioResponseDTO login(LoginRequestDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByMail(loginDTO.getMail()).orElse(null);
        if (usuario == null || !usuario.getContrasena().equals(loginDTO.getContrasena())) {
            throw UsuarioException.credencialesIncorrectas();
        }
        return convertirADTO(usuario);
    }

    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> UsuarioException.noEncontrado(id));

        // Solo valido unicidad si el campo efectivamente cambió
        if (!usuario.getMail().equals(usuarioDTO.getMail()) && usuarioRepository.existsByMail(usuarioDTO.getMail())) {
            throw UsuarioException.mailYaRegistrado(usuarioDTO.getMail());
        }
        if (!usuario.getNombreUsuario().equals(usuarioDTO.getNombreUsuario())
                && usuarioRepository.existsByNombreUsuario(usuarioDTO.getNombreUsuario())) {
            throw UsuarioException.nombreUsuarioYaRegistrado(usuarioDTO.getNombreUsuario());
        }
        validarFechaNacimientoYSexo(usuarioDTO);

        usuario.setNombreUsuario(usuarioDTO.getNombreUsuario());
        usuario.setMail(usuarioDTO.getMail());
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setFechaNacimiento(usuarioDTO.getFechaNacimiento());
        usuario.setSexo(usuarioDTO.getSexo());

        // Si no mandan contraseña, conservo la actual
        if (usuarioDTO.getContrasena() != null && !usuarioDTO.getContrasena().isBlank()) {
            usuario.setContrasena(usuarioDTO.getContrasena());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return convertirADTO(actualizado);
    }

    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw UsuarioException.noEncontrado(id);
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO convertirADTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombreUsuario(usuario.getNombreUsuario());
        dto.setMail(usuario.getMail());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setFechaNacimiento(usuario.getFechaNacimiento());
        dto.setSexo(usuario.getSexo());
        return dto;
    }
}
