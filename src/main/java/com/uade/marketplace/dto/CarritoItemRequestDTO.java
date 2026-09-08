package com.uade.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemRequestDTO {
    private Long usuarioId;
    private Long productoId;
    private Integer cantidad;
}