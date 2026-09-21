package com.uade.marketplace.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDTO {
    private Long pedidoId;
    private Long usuarioId;
    private LocalDateTime fecha;
    private Double total;
    private List<PedidoItemResponseDTO> items;
}