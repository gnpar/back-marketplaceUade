package com.uade.marketplace.service;

import com.uade.marketplace.dto.CarritoItemRequestDTO;
import com.uade.marketplace.dto.CarritoItemResponseDTO;
import com.uade.marketplace.exception.CarritoException;
import com.uade.marketplace.exception.ProductoException;
import com.uade.marketplace.exception.UsuarioException;
import com.uade.marketplace.model.CarritoItem;
import com.uade.marketplace.model.Producto;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.CarritoItemRepository;
import com.uade.marketplace.repository.ProductoRepository;
import com.uade.marketplace.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CarritoService {

    private final CarritoItemRepository carritoItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public CarritoService(CarritoItemRepository carritoItemRepository, UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository) {
        this.carritoItemRepository = carritoItemRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    public List<CarritoItemResponseDTO> getCarrito(Long usuarioId) {
        List<CarritoItem> items = carritoItemRepository.findByUsuarioId(usuarioId);
        List<CarritoItemResponseDTO> dtos = new ArrayList<>();
        for (CarritoItem item : items) {
            dtos.add(convertirADTO(item));
        }
        return dtos;
    }

    public CarritoItemResponseDTO agregarItem(CarritoItemRequestDTO itemDTO) {
        Usuario usuario = usuarioRepository.findById(itemDTO.getUsuarioId())
                .orElseThrow(() -> UsuarioException.noEncontrado(itemDTO.getUsuarioId()));
        Producto producto = productoRepository.findById(itemDTO.getProductoId())
                .orElseThrow(() -> ProductoException.noEncontrado(itemDTO.getProductoId()));

        int cantidad = itemDTO.getCantidad() != null ? itemDTO.getCantidad() : 1;
        if (cantidad <= 0) {
            throw CarritoException.cantidadInvalida();
        }

        CarritoItem item = carritoItemRepository.findByUsuarioIdAndProductoId(usuario.getId(), producto.getId())
                .orElse(null);

        int cantidadTotal = cantidad + (item != null ? item.getCantidad() : 0);
        if (producto.getStock() == null || producto.getStock() < cantidadTotal) {
            throw ProductoException.sinStock(producto.getId());
        }

        if (item == null) {
            item = new CarritoItem();
            item.setUsuario(usuario);
            item.setProducto(producto);
            item.setCantidad(cantidad);
        } else {
            item.setCantidad(item.getCantidad() + cantidad);
        }

        CarritoItem guardado = carritoItemRepository.save(item);
        return convertirADTO(guardado);
    }

    public void quitarItem(Long id) {
        if (!carritoItemRepository.existsById(id)) {
            throw CarritoException.noEncontrado(id);
        }
        carritoItemRepository.deleteById(id);
    }

    private CarritoItemResponseDTO convertirADTO(CarritoItem item) {
        CarritoItemResponseDTO dto = new CarritoItemResponseDTO();
        dto.setId(item.getId());
        dto.setUsuarioId(item.getUsuario().getId());
        dto.setProductoId(item.getProducto().getId());
        dto.setNombreProducto(item.getProducto().getNombre());
        dto.setPrecio(item.getProducto().getPrecio());
        dto.setCantidad(item.getCantidad());
        return dto;
    }

    public Double checkout(Long usuarioId) {
        List<CarritoItem> items = carritoItemRepository.findByUsuarioId(usuarioId);
        if (items.isEmpty()) {
            throw CarritoException.carritoVacio(usuarioId);
        }

        // Validar stock de todos los items antes de descontar nada
        for (CarritoItem item : items) {
            Producto producto = item.getProducto();
            if (producto.getStock() == null || producto.getStock() < item.getCantidad()) {
                throw ProductoException.sinStock(producto.getId());
            }
        }

        // Calcular total y descontar stock
        double total = 0.0;
        for (CarritoItem item : items) {
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
            total += producto.getPrecio() * item.getCantidad();
        }

        // Vaciar el carrito
        carritoItemRepository.deleteAll(items);

        return total;
    }
}