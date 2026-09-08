package com.uade.marketplace.repository;

import com.uade.marketplace.model.CarritoItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    List<CarritoItem> findByUsuarioId(Long usuarioId);

    Optional<CarritoItem> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
}