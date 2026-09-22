package com.uade.marketplace.controller;

import com.uade.marketplace.dto.ProductoRequestDTO;
import com.uade.marketplace.dto.ProductoResponseDTO;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.service.ProductoService;
import com.uade.marketplace.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// http://localhost:8080/api/productos
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    ProductoController(ProductoService productoService, UsuarioService usuarioService) {
        this.productoService = productoService;
        this.usuarioService = usuarioService;
    }

    // get http://localhost:8080/api/productos
    // get http://localhost:8080/api/productos?categoriaId=1
    // get http://localhost:8080/api/productos?usuarioId=1
    // get http://localhost:8080/api/productos?categoriaId=1&usuarioId=1
    @GetMapping()
    public ResponseEntity<List<ProductoResponseDTO>> getAllProductos(
            @RequestParam(required = false) @Positive(message = "El identificador debe ser mayor a cero") Long categoriaId,
            @RequestParam(required = false) @Positive(message = "El identificador debe ser mayor a cero") Long usuarioId) {
        return ResponseEntity.ok(productoService.getProductos(categoriaId, usuarioId));
    }

    // get http://localhost:8080/api/productos/1
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> getProductoById(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long id) {
        return ResponseEntity.ok(productoService.getProductoById(id));
    }

    // post http://localhost:8080/api/productos (usuario autenticado = vendedor)
    @PostMapping()
    public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO productoDTO,
            Principal principal) {
        Usuario usuario = usuarioService.obtenerAutenticado(principal);
        ProductoResponseDTO creado = productoService.crearProducto(productoDTO, usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // put http://localhost:8080/api/productos/1 (solo el vendedor que lo creo)
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long id,
            @Valid @RequestBody ProductoRequestDTO productoDTO, Principal principal) {
        Usuario usuario = usuarioService.obtenerAutenticado(principal);
        ProductoResponseDTO actualizado = productoService.actualizarProducto(id, productoDTO, usuario.getId());
        return ResponseEntity.ok(actualizado);
    }

    // delete http://localhost:8080/api/productos/1 (solo el vendedor que lo creo)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long id, Principal principal) {
        Usuario usuario = usuarioService.obtenerAutenticado(principal);
        productoService.eliminarProducto(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
