package com.uade.marketplace.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String nombre;
}
