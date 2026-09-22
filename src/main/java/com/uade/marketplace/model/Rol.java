package com.uade.marketplace.model;

/**
 * Rol de un usuario dentro del marketplace.
 *
 * <p>
 * Se traduce a una autoridad de Spring Security con el prefijo {@code ROLE_}
 * (por ejemplo {@code ROLE_ADMIN}), que es lo que espera
 * {@code hasRole("ADMIN")} en la configuracion de permisos por ruta.
 * </p>
 */
public enum Rol {
    /** Usuario comun: compra y publica sus propios productos. */
    USUARIO,

    /** Administrador del sitio: ademas gestiona el catalogo de categorias. */
    ADMIN
}
