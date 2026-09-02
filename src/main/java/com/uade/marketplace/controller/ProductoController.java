package com.uade.marketplace.controller;

import com.uade.marketplace.dto.ProductoRequestDTO;
import com.uade.marketplace.dto.ProductoResponseDTO;
import com.uade.marketplace.service.ProductoService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// http://localhost:8080/api/productos
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // get http://localhost:8080/api/productos
    @GetMapping()
    public List<ProductoResponseDTO> getAllProductos() {
        return productoService.getAllProductos();
    }

    // get http://localhost:8080/api/productos/1
    @GetMapping("/{id}")
    public ProductoResponseDTO getProductoById(@PathVariable Long id) {
        return productoService.getProductoById(id);
    }

    // post http://localhost:8080/api/productos
    @PostMapping()
    public ProductoResponseDTO crearProducto(@RequestBody ProductoRequestDTO productoDTO) {
        return productoService.crearProducto(productoDTO);
    }

    // put http://localhost:8080/api/productos/1
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(@PathVariable Long id,
            @RequestBody ProductoRequestDTO productoDTO) {
        ProductoResponseDTO actualizado = productoService.actualizarProducto(id, productoDTO);
        return ResponseEntity.ok(actualizado);
    }

    // delete http://localhost:8080/api/productos/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
