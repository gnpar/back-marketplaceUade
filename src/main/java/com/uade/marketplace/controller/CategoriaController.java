package com.uade.marketplace.controller;

import com.uade.marketplace.dto.CategoriaRequestDTO;
import com.uade.marketplace.dto.CategoriaResponseDTO;
import com.uade.marketplace.service.CategoriaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
import org.springframework.web.bind.annotation.RestController;

// http://localhost:8080/api/categorias
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // get http://localhost:8080/api/categorias
    @GetMapping()
    public ResponseEntity<List<CategoriaResponseDTO>> getAllCategorias() {
        return ResponseEntity.ok(categoriaService.getAllCategorias());
    }

    // get http://localhost:8080/api/categorias/1
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> getCategoriaById(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long id) {
        return ResponseEntity.ok(categoriaService.getCategoriaById(id));
    }

    // post http://localhost:8080/api/categorias
    @PostMapping()
    public ResponseEntity<CategoriaResponseDTO> crearCategoria(@Valid @RequestBody CategoriaRequestDTO categoriaDTO) {
        CategoriaResponseDTO creada = categoriaService.crearCategoria(categoriaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // put http://localhost:8080/api/categorias/1
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizarCategoria(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long id,
            @Valid @RequestBody CategoriaRequestDTO categoriaDTO) {
        return ResponseEntity.ok(categoriaService.actualizarCategoria(id, categoriaDTO));
    }

    // delete http://localhost:8080/api/categorias/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}