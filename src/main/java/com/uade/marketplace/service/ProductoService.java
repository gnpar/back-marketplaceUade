package com.uade.marketplace.service;

import com.uade.marketplace.dto.ProductoRequestDTO;
import com.uade.marketplace.dto.ProductoResponseDTO;
import com.uade.marketplace.model.Producto;
import com.uade.marketplace.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoResponseDTO> getAllProductos() {
        // select * from productos
        List<Producto> productos = productoRepository.findAll();
        List<ProductoResponseDTO> dtos = new ArrayList<>();
        for (Producto producto : productos) {
            dtos.add(convertirADTO(producto));
        }
        return dtos;
    }

    public ProductoResponseDTO getProductoById(Long id) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto == null) {
            return null;
        }
        return convertirADTO(producto);
    }

    public ProductoResponseDTO crearProducto(ProductoRequestDTO productoDTO) {
        Producto producto = new Producto();
        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio());

        Producto guardado = productoRepository.save(producto);

        return convertirADTO(guardado);
    }

    public ProductoResponseDTO actualizarProducto(Long id, ProductoRequestDTO productoDTO) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto == null) {
            return null;
        }
        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio());

        Producto guardado = productoRepository.save(producto);

        return convertirADTO(guardado);
    }

    public boolean eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            return false;
        }
        productoRepository.deleteById(id);
        return true;
    }

    private ProductoResponseDTO convertirADTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        return dto;
    }

}
