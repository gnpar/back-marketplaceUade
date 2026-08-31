package com.uade.marketplace.repository;

import com.uade.marketplace.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaRepository Provides CRUD operations and additional query methods for the
 * Producto save, update, delete, findById, findAll, etc. de la tabla productos
 * ProductoRepository
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

}
