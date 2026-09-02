package com.uade.marketplace.controller;

import com.uade.marketplace.dto.CategoriaRequestDTO;
import com.uade.marketplace.dto.CategoriaResponseDTO;
import com.uade.marketplace.service.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // put http://localhost:8080/api/categorias/1
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizarCategoria(@PathVariable Long id,
            @RequestBody CategoriaRequestDTO categoriaDTO) {
        CategoriaResponseDTO actualizada = categoriaService.actualizarCategoria(id, categoriaDTO);
        if (actualizada == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(actualizada);
    }

    // delete http://localhost:8080/api/categorias/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        boolean eliminado = categoriaService.eliminarCategoria(id);
        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}