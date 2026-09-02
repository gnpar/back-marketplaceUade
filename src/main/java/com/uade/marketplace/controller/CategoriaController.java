package com.uade.marketplace.controller;

import com.uade.marketplace.dto.CategoriaRequestDTO;
import com.uade.marketplace.dto.CategoriaResponseDTO;
import com.uade.marketplace.service.CategoriaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public List<CategoriaResponseDTO> getAllCategorias() {
        return categoriaService.getAllCategorias();
    }

    // get http://localhost:8080/api/categorias/1
    @GetMapping("/{id}")
    public CategoriaResponseDTO getCategoriaById(@PathVariable Long id) {
        return categoriaService.getCategoriaById(id);
    }

    // post http://localhost:8080/api/categorias
    @PostMapping()
    public CategoriaResponseDTO crearCategoria(@RequestBody CategoriaRequestDTO categoriaDTO) {
        return categoriaService.crearCategoria(categoriaDTO);
    }
}