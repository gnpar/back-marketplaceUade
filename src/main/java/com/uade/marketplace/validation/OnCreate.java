package com.uade.marketplace.validation;

/**
 * Grupo de validacion para operaciones de alta (registro).
 *
 * <p>
 * Permite reutilizar {@code UsuarioRequestDTO} tanto en el registro como en la
 * actualizacion: las restricciones marcadas con este grupo (por ejemplo, que la
 * contrasena sea obligatoria) solo se aplican al crear el usuario, mientras que
 * al actualizar la contrasena puede omitirse para conservar la actual.
 * </p>
 */
public interface OnCreate {
}