package com.uade.marketplace.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "imagenes_producto")
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombreArchivo;

    @Column(nullable = false, length = 50)
    private String tipoContenido;

    @Column(nullable = false)
    private Long tamanio;

    // LONG32VARBINARY + capacidad máxima evita el TINYBLOB que Hibernate elige
    // con la capacidad por defecto de 255: en MySQL genera LONGBLOB (y BLOB en H2).
    @JdbcTypeCode(SqlTypes.LONG32VARBINARY)
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, length = Integer.MAX_VALUE)
    private byte[] datos;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}
