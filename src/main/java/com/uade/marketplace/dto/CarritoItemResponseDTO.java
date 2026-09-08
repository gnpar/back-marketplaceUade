package com.uade.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemResponseDTO {
    private Long id;
    private Long usuarioId;
    private Long productoId;
    private String nombreProducto;
    private Double precio;
    private Integer cantidad;
}