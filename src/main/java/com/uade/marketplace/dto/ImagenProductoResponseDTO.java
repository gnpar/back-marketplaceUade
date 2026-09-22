package com.uade.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenProductoResponseDTO {
    private Long id;
    private String nombreArchivo;
    private String tipoContenido;
    private Long tamanio;
    private String url;
}
