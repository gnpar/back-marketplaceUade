package com.uade.marketplace.service;

import com.uade.marketplace.dto.LoginRequestDTO;
import com.uade.marketplace.dto.LoginResponseDTO;
import com.uade.marketplace.dto.UsuarioRequestDTO;
import com.uade.marketplace.dto.UsuarioResponseDTO;
import com.uade.marketplace.exception.UsuarioException;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.UsuarioRepository;
import com.uade.marketplace.security.JwtService;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

        // La contrasena nunca se guarda en texto plano: se almacena su hash BCrypt.
        Usuario usuario = Usuario.builder().nombreUsuario(usuarioDTO.getNombreUsuario()).mail(usuarioDTO.getMail())
                .contrasena(passwordEncoder.encode(usuarioDTO.getContrasena())).nombre(usuarioDTO.getNombre())
                .apellido(usuarioDTO.getApellido()).fechaNacimiento(usuarioDTO.getFechaNacimiento())
                .sexo(usuarioDTO.getSexo()).build();

        Usuario guardado = usuarioRepository.save(usuario);
        return convertirADTO(guardado);
    }

    public LoginResponseDTO login(LoginRequestDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByMail(loginDTO.getMail()).orElse(null);
        if (usuario == null || !passwordEncoder.matches(loginDTO.getContrasena(), usuario.getContrasena())) {
            throw UsuarioException.credencialesIncorrectas();
        }
        String token = jwtService.generarToken(usuario.getMail());
        return new LoginResponseDTO(convertirADTO(usuario), token);
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

        usuario.setNombreUsuario(usuarioDTO.getNombreUsuario());
        usuario.setMail(usuarioDTO.getMail());
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setFechaNacimiento(usuarioDTO.getFechaNacimiento());
        usuario.setSexo(usuarioDTO.getSexo());

        // Si no mandan contrasena, conservo la actual; si mandan, guardo su hash.
        if (usuarioDTO.getContrasena() != null && !usuarioDTO.getContrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(usuarioDTO.getContrasena()));
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

    // Resuelve el usuario autenticado a partir del Principal que deja el filtro
    // de JWT (el nombre del principal es el mail, subject del token).
    public Usuario obtenerAutenticado(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw UsuarioException.noAutenticado();
        }
        return usuarioRepository.findByMail(principal.getName()).orElseThrow(UsuarioException::noAutenticado);
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