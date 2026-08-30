package com.uade.e_commerce.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor  


public class ProductoRequestDTO {
    private String nombre;
    private String descripcion;
    private Double precio;
    
}
