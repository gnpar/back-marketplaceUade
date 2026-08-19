package com.uade.e_commerce.service;

import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

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
    public Producto getProductoById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }
    public Producto crearProducto(Producto producto) {
        return productoRepository.save(producto);
    }

}
