package com.uade.marketplace.repository;

import com.uade.marketplace.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaRepository Provides CRUD operations and additional query methods for the
 * Categoria save, update, delete, findById, findAll, etc. de la tabla
 * categorias CategoriaRepository
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombre(String nombre);
}