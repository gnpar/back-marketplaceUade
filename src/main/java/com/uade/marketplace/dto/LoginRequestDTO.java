package com.uade.marketplace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    @NotBlank(message = "El mail es obligatorio")
    @Email(message = "El mail no tiene un formato valido")
    private String mail;
    @NotBlank(message = "La contrasena es obligatoria")
    private String contrasena;
}
