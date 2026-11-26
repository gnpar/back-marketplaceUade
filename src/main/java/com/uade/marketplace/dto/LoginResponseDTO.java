package com.uade.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta del login. El JWT NO viaja aca: el back lo manda en la cookie
 * HttpOnly {@code marketplace_token}, que JavaScript no puede leer.
 */
@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private UsuarioResponseDTO usuario;
}
