package com.uade.e_commerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.ProductoRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> getAllProductos() {
        // select * from productos
        return productoRepository.findAll();
    }


    
}
