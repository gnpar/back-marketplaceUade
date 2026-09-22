package com.uade.marketplace.controller;

import com.uade.marketplace.dto.LoginRequestDTO;
import com.uade.marketplace.dto.LoginResponseDTO;
import com.uade.marketplace.dto.UsuarioRequestDTO;
import com.uade.marketplace.dto.UsuarioResponseDTO;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.service.UsuarioService;
import com.uade.marketplace.validation.OnCreate;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }

    // get http://localhost:8080/api/usuarios/1
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.getUsuarioById(id));
    }

    // post http://localhost:8080/api/usuarios/registro
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(
            @Validated({OnCreate.class, Default.class}) @RequestBody UsuarioRequestDTO usuarioDTO) {
        UsuarioResponseDTO creado = usuarioService.crearUsuario(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // post http://localhost:8080/api/usuarios/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginDTO) {
        return ResponseEntity.ok(usuarioService.login(loginDTO));
    }

    // put http://localhost:8080/api/usuarios/1 (la propia cuenta, o un ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(@PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO usuarioDTO, Principal principal) {
        Usuario solicitante = usuarioService.obtenerAutenticado(principal);
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuarioDTO, solicitante));
    }

    // delete http://localhost:8080/api/usuarios/1 (la propia cuenta, o un ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id, Principal principal) {
        Usuario solicitante = usuarioService.obtenerAutenticado(principal);
        usuarioService.eliminarUsuario(id, solicitante);
        return ResponseEntity.noContent().build();
    }
}