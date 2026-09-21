package com.uade.marketplace.service;

import com.uade.marketplace.dto.CarritoItemRequestDTO;
import com.uade.marketplace.dto.CarritoItemResponseDTO;
import com.uade.marketplace.dto.CheckoutResponseDTO;
import com.uade.marketplace.dto.PedidoItemResponseDTO;
import com.uade.marketplace.exception.CarritoException;
import com.uade.marketplace.exception.ProductoException;
import com.uade.marketplace.exception.UsuarioException;
import com.uade.marketplace.model.CarritoItem;
import com.uade.marketplace.model.Pedido;
import com.uade.marketplace.model.PedidoItem;
import com.uade.marketplace.model.Producto;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.CarritoItemRepository;
import com.uade.marketplace.repository.PedidoRepository;
import com.uade.marketplace.repository.ProductoRepository;
import com.uade.marketplace.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CarritoService {

    private final CarritoItemRepository carritoItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    public CarritoService(CarritoItemRepository carritoItemRepository, UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository, PedidoRepository pedidoRepository) {
        this.carritoItemRepository = carritoItemRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
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

    public void quitarItem(Long usuarioId, Long itemId) {
        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> CarritoException.noEncontrado(itemId));
        if (!item.getUsuario().getId().equals(usuarioId)) {
            throw CarritoException.noPerteneceAlUsuario(itemId);
        }
        carritoItemRepository.deleteById(itemId);
    }

    public void vaciarCarrito(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw UsuarioException.noEncontrado(usuarioId);
        }
        carritoItemRepository.deleteAll(carritoItemRepository.findByUsuarioId(usuarioId));
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

    public CheckoutResponseDTO checkout(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> UsuarioException.noEncontrado(usuarioId));

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

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFecha(LocalDateTime.now());

        // Calcular total, descontar stock y armar los items del pedido
        double total = 0.0;
        for (CarritoItem item : items) {
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            total += producto.getPrecio() * item.getCantidad();

            PedidoItem pedidoItem = new PedidoItem();
            pedidoItem.setPedido(pedido);
            pedidoItem.setProducto(producto);
            pedidoItem.setCantidad(item.getCantidad());
            pedidoItem.setPrecioUnitario(producto.getPrecio());
            pedido.getItems().add(pedidoItem);
        }

        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        // Vaciar el carrito
        carritoItemRepository.deleteAll(items);

        return convertirPedidoADTO(pedido);
    }

    private CheckoutResponseDTO convertirPedidoADTO(Pedido pedido) {
        CheckoutResponseDTO dto = new CheckoutResponseDTO();
        dto.setPedidoId(pedido.getId());
        dto.setUsuarioId(pedido.getUsuario().getId());
        dto.setFecha(pedido.getFecha());
        dto.setTotal(pedido.getTotal());

        List<PedidoItemResponseDTO> itemsDTO = new ArrayList<>();
        for (PedidoItem item : pedido.getItems()) {
            PedidoItemResponseDTO itemDTO = new PedidoItemResponseDTO();
            itemDTO.setProductoId(item.getProducto().getId());
            itemDTO.setNombreProducto(item.getProducto().getNombre());
            itemDTO.setCantidad(item.getCantidad());
            itemDTO.setPrecioUnitario(item.getPrecioUnitario());
            itemsDTO.add(itemDTO);
        }
        dto.setItems(itemsDTO);
        return dto;
    }
}