package com.uade.e_commerce.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.service.ProductoService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



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
    public List<Producto> getAllProductos() {
        return productoService.getAllProductos();
    }

    // get http://localhost:8080/api/productos/1
    @GetMapping("/{id}")
    public Producto getProductoById(@PathVariable Long id) {
        return null;
    }
    
    
}
