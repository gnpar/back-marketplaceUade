package com.uade.marketplace.dto;

import java.time.LocalDateTime;

public class ErrorResponseDTO {

    private int status;
    private String mensaje;
    private LocalDateTime timestamp;

    public ErrorResponseDTO(int status, String mensaje) {
        this.status = status;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() {
        return status;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}