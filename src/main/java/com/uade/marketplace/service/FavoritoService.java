package com.uade.marketplace.service;

import com.uade.marketplace.dto.ProductoResponseDTO;
import com.uade.marketplace.exception.ProductoException;
import com.uade.marketplace.exception.UsuarioException;
import com.uade.marketplace.model.Favorito;
import com.uade.marketplace.model.Producto;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.FavoritoRepository;
import com.uade.marketplace.repository.ProductoRepository;
import com.uade.marketplace.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public FavoritoService(FavoritoRepository favoritoRepository, UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    public List<ProductoResponseDTO> getFavoritos(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw UsuarioException.noEncontrado(usuarioId);
        }
        List<ProductoResponseDTO> dtos = new ArrayList<>();
        for (Favorito favorito : favoritoRepository.findByUsuarioIdOrderByIdAsc(usuarioId)) {
            dtos.add(convertirADTO(favorito.getProducto()));
        }
        return dtos;
    }

    // Es idempotente: si el producto ya estaba en favoritos, no lo duplica.
    public ProductoResponseDTO agregarFavorito(Long usuarioId, Long productoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> UsuarioException.noEncontrado(usuarioId));
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> ProductoException.noEncontrado(productoId));

        if (!favoritoRepository.existsByUsuarioIdAndProductoId(usuarioId, productoId)) {
            Favorito favorito = new Favorito();
            favorito.setUsuario(usuario);
            favorito.setProducto(producto);
            favoritoRepository.save(favorito);
        }

        return convertirADTO(producto);
    }

    // Tambien es idempotente: si ya no estaba, no hay nada que borrar.
    public void quitarFavorito(Long usuarioId, Long productoId) {
        favoritoRepository.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
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
