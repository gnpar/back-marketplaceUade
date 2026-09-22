package com.uade.marketplace.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemRequestDTO {
    @NotNull(message = "El producto es obligatorio")
    @Positive(message = "El identificador de producto debe ser mayor a cero")
    private Long productoId;
    // Si se omite, el servicio conserva la cantidad predeterminada de uno.
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;
}
