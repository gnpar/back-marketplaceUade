package com.uade.marketplace.controller;

import com.uade.marketplace.dto.LoginRequestDTO;
import com.uade.marketplace.dto.UsuarioRequestDTO;
import com.uade.marketplace.dto.UsuarioResponseDTO;
import com.uade.marketplace.service.UsuarioService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

// http://localhost:8080/api/usuarios
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // get http://localhost:8080/api/usuarios
    @GetMapping()
    public List<UsuarioResponseDTO> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    // get http://localhost:8080/api/usuarios/1
    @GetMapping("/{id}")
    public UsuarioResponseDTO getUsuarioById(@PathVariable Long id) {
        return usuarioService.getUsuarioById(id);
    }

    // post http://localhost:8080/api/usuarios/registro
    @PostMapping("/registro")
    public UsuarioResponseDTO registrar(@RequestBody UsuarioRequestDTO usuarioDTO) {
        return usuarioService.crearUsuario(usuarioDTO);
    }

    // post http://localhost:8080/api/usuarios/login
    @PostMapping("/login")
    public UsuarioResponseDTO login(@RequestBody LoginRequestDTO loginDTO) {
        return usuarioService.login(loginDTO);
    }

    // put http://localhost:8080/api/usuarios/1
    @PutMapping("/{id}")
    public UsuarioResponseDTO actualizarUsuario(
            @PathVariable Long id, @RequestBody UsuarioRequestDTO usuarioDTO) {
        return usuarioService.actualizarUsuario(id, usuarioDTO);
    }

    // delete http://localhost:8080/api/usuarios/1
    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }
}