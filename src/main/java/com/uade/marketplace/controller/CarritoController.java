package com.uade.marketplace.controller;

import com.uade.marketplace.dto.CarritoItemRequestDTO;
import com.uade.marketplace.dto.CarritoItemResponseDTO;
import com.uade.marketplace.dto.CheckoutResponseDTO;
import com.uade.marketplace.service.CarritoService;
import com.uade.marketplace.service.UsuarioService;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// http://localhost:8080/api/carrito
@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final UsuarioService usuarioService;

    CarritoController(CarritoService carritoService, UsuarioService usuarioService) {
        this.carritoService = carritoService;
        this.usuarioService = usuarioService;
    }

    // get http://localhost:8080/api/carrito
    @GetMapping()
    public ResponseEntity<List<CarritoItemResponseDTO>> getCarrito(Principal principal) {
        return ResponseEntity.ok(carritoService.getCarrito(usuarioId(principal)));
    }

    // post http://localhost:8080/api/carrito
    @PostMapping()
    public ResponseEntity<CarritoItemResponseDTO> agregarItem(@RequestBody CarritoItemRequestDTO itemDTO,
            Principal principal) {
        CarritoItemResponseDTO agregado = carritoService.agregarItem(itemDTO, usuarioId(principal));
        return ResponseEntity.status(HttpStatus.CREATED).body(agregado);
    }

    // delete http://localhost:8080/api/carrito/items/5
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> quitarItem(@PathVariable Long itemId, Principal principal) {
        carritoService.quitarItem(usuarioId(principal), itemId);
        return ResponseEntity.noContent().build();
    }

    // delete http://localhost:8080/api/carrito (vaciar carrito sin comprar)
    @DeleteMapping()
    public ResponseEntity<Void> vaciarCarrito(Principal principal) {
        carritoService.vaciarCarrito(usuarioId(principal));
        return ResponseEntity.noContent().build();
    }

    // post http://localhost:8080/api/carrito/checkout
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDTO> checkout(Principal principal) {
        CheckoutResponseDTO checkoutDTO = carritoService.checkout(usuarioId(principal));
        return ResponseEntity.ok(checkoutDTO);
    }

    // El usuario sale del Principal que deja el filtro de JWT (mail autenticado).
    private Long usuarioId(Principal principal) {
        return usuarioService.obtenerAutenticado(principal).getId();
    }
}
