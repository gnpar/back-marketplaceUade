package com.uade.marketplace.service;

import com.uade.marketplace.dto.ProductoRequestDTO;
import com.uade.marketplace.dto.ProductoResponseDTO;
import com.uade.marketplace.exception.CategoriaException;
import com.uade.marketplace.exception.ProductoException;
import com.uade.marketplace.exception.UsuarioException;
import com.uade.marketplace.model.Categoria;
import com.uade.marketplace.model.Producto;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.CategoriaRepository;
import com.uade.marketplace.repository.ProductoRepository;
import com.uade.marketplace.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ProductoResponseDTO> getProductos(Long categoriaId, Long usuarioId) {
        if (usuarioId != null) {
            validarUsuario(usuarioId);
        }
        if (categoriaId != null) {
            validarCategoria(categoriaId);
        }
        if (usuarioId != null && categoriaId != null) {
            return convertirADTOs(
                    productoRepository.findByUsuarioIdAndCategoriaIdOrderByNombreAsc(usuarioId, categoriaId));
        }
        if (usuarioId != null) {
            return convertirADTOs(productoRepository.findByUsuarioIdOrderByNombreAsc(usuarioId));
        }
        if (categoriaId != null) {
            return convertirADTOs(productoRepository.findByCategoriaIdOrderByNombreAsc(categoriaId));
        }
        return convertirADTOs(productoRepository.findAllByOrderByNombreAsc());
    }

    public ProductoResponseDTO getProductoById(Long id) {
        Producto producto = productoRepository.findById(id).orElseThrow(() -> ProductoException.noEncontrado(id));
        return convertirADTO(producto);
    }

    public ProductoResponseDTO crearProducto(ProductoRequestDTO productoDTO, Long usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);
        Categoria categoria = obtenerCategoria(productoDTO.getCategoriaId());

        Producto producto = new Producto();
        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio());
        producto.setStock(productoDTO.getStock());
        producto.setCategoria(categoria);
        producto.setUsuario(usuario);

        Producto guardado = productoRepository.save(producto);

        return convertirADTO(guardado);
    }

    public ProductoResponseDTO actualizarProducto(Long id, ProductoRequestDTO productoDTO, Long usuarioId) {
        Producto producto = productoRepository.findById(id).orElseThrow(() -> ProductoException.noEncontrado(id));
        validarPropietario(producto, usuarioId);
        Categoria categoria = obtenerCategoria(productoDTO.getCategoriaId());

        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecio(productoDTO.getPrecio());
        producto.setStock(productoDTO.getStock());
        producto.setCategoria(categoria);

        Producto guardado = productoRepository.save(producto);

        return convertirADTO(guardado);
    }

    public void eliminarProducto(Long id, Long usuarioId) {
        Producto producto = productoRepository.findById(id).orElseThrow(() -> ProductoException.noEncontrado(id));
        validarPropietario(producto, usuarioId);
        productoRepository.delete(producto);
    }

    private Usuario obtenerUsuario(Long usuarioId) {
        if (usuarioId == null) {
            throw ProductoException.datosInvalidos("El usuario es obligatorio");
        }
        return usuarioRepository.findById(usuarioId).orElseThrow(() -> UsuarioException.noEncontrado(usuarioId));
    }

    private void validarUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw UsuarioException.noEncontrado(usuarioId);
        }
    }

    private void validarCategoria(Long categoriaId) {
        if (!categoriaRepository.existsById(categoriaId)) {
            throw CategoriaException.noEncontrada(categoriaId);
        }
    }

    // Solo el usuario que publico el producto puede modificarlo o eliminarlo.
    private void validarPropietario(Producto producto, Long usuarioId) {
        if (usuarioId == null) {
            throw UsuarioException.noAutenticado();
        }
        if (!producto.getUsuario().getId().equals(usuarioId)) {
            throw ProductoException.noPerteneceAlUsuario(producto.getId());
        }
    }

    private Categoria obtenerCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw ProductoException.datosInvalidos("La categoría es obligatoria");
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> CategoriaException.noEncontrada(categoriaId));
    }

    private List<ProductoResponseDTO> convertirADTOs(List<Producto> productos) {
        List<ProductoResponseDTO> dtos = new ArrayList<>();
        for (Producto producto : productos) {
            dtos.add(convertirADTO(producto));
        }
        return dtos;
    }

    private ProductoResponseDTO convertirADTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setCategoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null);
        dto.setUsuarioId(producto.getUsuario().getId());
        return dto;
    }
}
