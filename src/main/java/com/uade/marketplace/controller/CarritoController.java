package com.uade.marketplace.controller;

import com.uade.marketplace.dto.CarritoItemRequestDTO;
import com.uade.marketplace.dto.CarritoItemResponseDTO;
import com.uade.marketplace.dto.CheckoutResponseDTO;
import com.uade.marketplace.service.CarritoService;
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

    CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    // get http://localhost:8080/api/carrito/1
    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<CarritoItemResponseDTO>> getCarrito(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(carritoService.getCarrito(usuarioId));
    }

    // post http://localhost:8080/api/carrito
    @PostMapping()
    public ResponseEntity<CarritoItemResponseDTO> agregarItem(@RequestBody CarritoItemRequestDTO itemDTO) {
        CarritoItemResponseDTO agregado = carritoService.agregarItem(itemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(agregado);
    }

    // delete http://localhost:8080/api/carrito/1/items/5
    @DeleteMapping("/{usuarioId}/items/{itemId}")
    public ResponseEntity<Void> quitarItem(@PathVariable Long usuarioId, @PathVariable Long itemId) {
        carritoService.quitarItem(usuarioId, itemId);
        return ResponseEntity.noContent().build();
    }

    // delete http://localhost:8080/api/carrito/1 (vaciar carrito sin comprar)
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> vaciarCarrito(@PathVariable Long usuarioId) {
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
    }

    // post http://localhost:8080/api/carrito/1/checkout
    @PostMapping("/{usuarioId}/checkout")
    public ResponseEntity<CheckoutResponseDTO> checkout(@PathVariable Long usuarioId) {
        CheckoutResponseDTO checkoutDTO = carritoService.checkout(usuarioId);
        return ResponseEntity.ok(checkoutDTO);
    }
}