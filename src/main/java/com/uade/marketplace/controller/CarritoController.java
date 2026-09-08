package com.uade.marketplace.controller;

import com.uade.marketplace.dto.CarritoItemRequestDTO;
import com.uade.marketplace.dto.CarritoItemResponseDTO;
import com.uade.marketplace.service.CarritoService;
import java.util.List;
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
    public List<CarritoItemResponseDTO> getCarrito(@PathVariable Long usuarioId) {
        return carritoService.getCarrito(usuarioId);
    }

    // post http://localhost:8080/api/carrito
    @PostMapping()
    public CarritoItemResponseDTO agregarItem(@RequestBody CarritoItemRequestDTO itemDTO) {
        return carritoService.agregarItem(itemDTO);
    }

    // delete http://localhost:8080/api/carrito/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> quitarItem(@PathVariable Long id) {
        carritoService.quitarItem(id);
        return ResponseEntity.noContent().build();
    }
}