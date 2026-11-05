package com.uade.marketplace.controller;

import com.uade.marketplace.dto.ProductoResponseDTO;
import com.uade.marketplace.service.FavoritoService;
import com.uade.marketplace.service.UsuarioService;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// http://localhost:8080/api/favoritos
@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;
    private final UsuarioService usuarioService;

    FavoritoController(FavoritoService favoritoService, UsuarioService usuarioService) {
        this.favoritoService = favoritoService;
        this.usuarioService = usuarioService;
    }

    // get http://localhost:8080/api/favoritos
    @GetMapping()
    public ResponseEntity<List<ProductoResponseDTO>> getFavoritos(Principal principal) {
        return ResponseEntity.ok(favoritoService.getFavoritos(usuarioId(principal)));
    }

    // post http://localhost:8080/api/favoritos/5
    @PostMapping("/{productoId}")
    public ResponseEntity<ProductoResponseDTO> agregarFavorito(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long productoId,
            Principal principal) {
        ProductoResponseDTO agregado = favoritoService.agregarFavorito(usuarioId(principal), productoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(agregado);
    }

    // delete http://localhost:8080/api/favoritos/5
    @DeleteMapping("/{productoId}")
    public ResponseEntity<Void> quitarFavorito(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long productoId,
            Principal principal) {
        favoritoService.quitarFavorito(usuarioId(principal), productoId);
        return ResponseEntity.noContent().build();
    }

    // El usuario sale del Principal que deja el filtro de JWT (mail autenticado).
    private Long usuarioId(Principal principal) {
        return usuarioService.obtenerAutenticado(principal).getId();
    }
}
