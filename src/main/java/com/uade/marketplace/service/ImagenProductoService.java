package com.uade.marketplace.service;

import com.uade.marketplace.dto.ImagenProductoResponseDTO;
import com.uade.marketplace.exception.ImagenProductoException;
import com.uade.marketplace.exception.ProductoException;
import com.uade.marketplace.exception.UsuarioException;
import com.uade.marketplace.model.ImagenProducto;
import com.uade.marketplace.model.Producto;
import com.uade.marketplace.repository.ImagenProductoRepository;
import com.uade.marketplace.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ImagenProductoService {

    public static final long TAMANIO_MAXIMO = 5L * 1024 * 1024;
    public static final int CANTIDAD_MAXIMA = 5;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final ImagenProductoRepository imagenRepository;
    private final ProductoRepository productoRepository;

    public ImagenProductoService(ImagenProductoRepository imagenRepository, ProductoRepository productoRepository) {
        this.imagenRepository = imagenRepository;
        this.productoRepository = productoRepository;
    }

    public ImagenProductoResponseDTO guardar(Long productoId, MultipartFile archivo, Long usuarioId) {
        Producto producto = obtenerProducto(productoId);
        validarPropietario(producto, usuarioId);
        validarArchivo(archivo);
        if (imagenRepository.countByProductoId(productoId) >= CANTIDAD_MAXIMA) {
            throw ImagenProductoException.limiteAlcanzado(CANTIDAD_MAXIMA);
        }

        try {
            byte[] datos = archivo.getBytes();
            validarFirma(datos, archivo.getContentType());
            ImagenProducto imagen = new ImagenProducto(null, limpiarNombre(archivo.getOriginalFilename()),
                    archivo.getContentType(), archivo.getSize(), datos, producto);
            return convertirADTO(imagenRepository.save(imagen));
        } catch (IOException ex) {
            throw ImagenProductoException.errorDeLectura();
        }
    }

    public List<ImagenProductoResponseDTO> listar(Long productoId) {
        obtenerProducto(productoId);
        return imagenRepository.findByProductoIdOrderByIdAsc(productoId).stream().map(this::convertirADTO).toList();
    }

    public ImagenProducto obtener(Long productoId, Long imagenId) {
        obtenerProducto(productoId);
        return imagenRepository.findByIdAndProductoId(imagenId, productoId)
                .orElseThrow(() -> ImagenProductoException.noEncontrada(imagenId));
    }

    public void eliminar(Long productoId, Long imagenId, Long usuarioId) {
        Producto producto = obtenerProducto(productoId);
        validarPropietario(producto, usuarioId);
        ImagenProducto imagen = imagenRepository.findByIdAndProductoId(imagenId, productoId)
                .orElseThrow(() -> ImagenProductoException.noEncontrada(imagenId));
        imagenRepository.delete(imagen);
    }

    private Producto obtenerProducto(Long productoId) {
        return productoRepository.findById(productoId).orElseThrow(() -> ProductoException.noEncontrado(productoId));
    }

    private void validarPropietario(Producto producto, Long usuarioId) {
        if (usuarioId == null) {
            throw UsuarioException.noAutenticado();
        }
        if (!producto.getUsuario().getId().equals(usuarioId)) {
            throw ProductoException.noPerteneceAlUsuario(producto.getId());
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw ImagenProductoException.archivoInvalido("La imagen es obligatoria y no puede estar vacía");
        }
        if (archivo.getSize() > TAMANIO_MAXIMO) {
            throw ImagenProductoException.archivoInvalido("La imagen no puede superar los 5 MB");
        }
        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) {
            throw ImagenProductoException.archivoInvalido("El formato debe ser JPEG, PNG o WebP");
        }
    }

    private void validarFirma(byte[] datos, String tipoContenido) {
        boolean valida = switch (tipoContenido) {
            case "image/jpeg" -> datos.length >= 3 && (datos[0] & 0xff) == 0xff && (datos[1] & 0xff) == 0xd8
                    && (datos[2] & 0xff) == 0xff;
            case "image/png" -> datos.length >= 8 && (datos[0] & 0xff) == 0x89 && datos[1] == 0x50 && datos[2] == 0x4e
                    && datos[3] == 0x47 && datos[4] == 0x0d && datos[5] == 0x0a && datos[6] == 0x1a && datos[7] == 0x0a;
            case "image/webp" -> datos.length >= 12 && coincide(datos, 0, "RIFF") && coincide(datos, 8, "WEBP");
            default -> false;
        };
        if (!valida) {
            throw ImagenProductoException.archivoInvalido("El contenido del archivo no coincide con su formato");
        }
    }

    private boolean coincide(byte[] datos, int inicio, String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (datos[inicio + i] != (byte) texto.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private String limpiarNombre(String nombre) {
        String limpio = nombre == null ? "imagen" : nombre.replace('\\', '/');
        limpio = limpio.substring(limpio.lastIndexOf('/') + 1).trim();
        if (limpio.isEmpty()) {
            limpio = "imagen";
        }
        return limpio.length() <= 255 ? limpio : limpio.substring(limpio.length() - 255);
    }

    private ImagenProductoResponseDTO convertirADTO(ImagenProducto imagen) {
        String url = "/api/productos/" + imagen.getProducto().getId() + "/imagenes/" + imagen.getId();
        return new ImagenProductoResponseDTO(imagen.getId(), imagen.getNombreArchivo(), imagen.getTipoContenido(),
                imagen.getTamanio(), url);
    }
}
