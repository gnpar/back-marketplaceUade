package com.uade.marketplace.dto;

import com.uade.marketplace.model.Sexo;
import com.uade.marketplace.validation.OnCreate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "El mail es obligatorio")
    @Email(message = "El mail no tiene un formato valido")
    private String mail;

    // @NotBlank solo se aplica al registrar (grupo OnCreate). Al actualizar la
    // contrasena puede omitirse: si viene null, @Size y @Pattern la dan por
    // valida y el servicio conserva la contrasena actual.
    @NotBlank(groups = OnCreate.class, message = "La contrasena es obligatoria")
    @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$", message = "La contrasena debe incluir al menos una letra y un numero")
    private String contrasena;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo;
}