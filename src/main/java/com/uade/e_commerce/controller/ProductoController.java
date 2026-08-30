package com.uade.e_commerce.controller;

import com.uade.e_commerce.dto.ProductoResponseDTO;
import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.service.ProductoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public Producto crearProducto(@RequestBody Producto producto) {
        return productoService.crearProducto(producto);
    }
}
