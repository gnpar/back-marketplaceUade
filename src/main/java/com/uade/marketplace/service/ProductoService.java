package com.uade.marketplace.service;

import com.uade.marketplace.dto.ProductoRequestDTO;
import com.uade.marketplace.dto.ProductoResponseDTO;
import com.uade.marketplace.exception.CategoriaException;
import com.uade.marketplace.exception.ProductoException;
import com.uade.marketplace.model.Categoria;
import com.uade.marketplace.model.Producto;
import com.uade.marketplace.repository.CategoriaRepository;
import com.uade.marketplace.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<ProductoResponseDTO> getAllProductos() {
        List<Producto> productos = productoRepository.findAllByOrderByNombreAsc();
        List<ProductoResponseDTO> dtos = new ArrayList<>();
        for (Producto producto : productos) {
            dtos.add(convertirADTO(producto));
        }
        return dtos;
    }

    public List<ProductoResponseDTO> getProductosByCategoria(Long categoriaId) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw CategoriaException.noEncontrada(categoriaId);
        }
        List<Producto> productos = productoRepository.findByCategoriaIdOrderByNombreAsc(categoriaId);
        List<ProductoResponseDTO> dtos = new ArrayList<>();
        for (Producto producto : productos) {
            dtos.add(convertirADTO(producto));
        }
        return dtos;
    }

    public ProductoResponseDTO getProductoById(Long id) {
        Producto producto = productoRepository.findById(id).orElseThrow(() -> ProductoException.noEncontrado(id));
        return convertirADTO(producto);
    }

    public ProductoResponseDTO crearProducto(ProductoRequestDTO productoDTO) {
        Categoria categoria = obtenerCategoria(productoDTO.getCategoriaId());

        Producto producto = new Producto();
        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio());
        producto.setStock(productoDTO.getStock());
        producto.setCategoria(categoria);

        Producto guardado = productoRepository.save(producto);

        return convertirADTO(guardado);
    }

    public ProductoResponseDTO actualizarProducto(Long id, ProductoRequestDTO productoDTO) {
        Producto producto = productoRepository.findById(id).orElseThrow(() -> ProductoException.noEncontrado(id));
        Categoria categoria = obtenerCategoria(productoDTO.getCategoriaId());

        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio());
        producto.setStock(productoDTO.getStock());
        producto.setCategoria(categoria);

        Producto guardado = productoRepository.save(producto);

        return convertirADTO(guardado);
    }

    private Categoria obtenerCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw ProductoException.datosInvalidos("La categoría es obligatoria");
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> CategoriaException.noEncontrada(categoriaId));
    }

    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw ProductoException.noEncontrado(id);
        }
        productoRepository.deleteById(id);
    }

    private ProductoResponseDTO convertirADTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setCategoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null);
        return dto;
    }
}