package com.uade.marketplace.repository;

import com.uade.marketplace.model.ImagenProducto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {
    List<ImagenProducto> findByProductoIdOrderByIdAsc(Long productoId);

    Optional<ImagenProducto> findByIdAndProductoId(Long id, Long productoId);

    long countByProductoId(Long productoId);

    void deleteByProductoId(Long productoId);
}
