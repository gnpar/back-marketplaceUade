package com.uade.marketplace.controller;

import com.uade.marketplace.dto.ImagenProductoResponseDTO;
import com.uade.marketplace.model.ImagenProducto;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.service.ImagenProductoService;
import com.uade.marketplace.service.UsuarioService;
import jakarta.validation.constraints.Positive;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/productos/{productoId}/imagenes")
public class ImagenProductoController {

    private final ImagenProductoService imagenService;
    private final UsuarioService usuarioService;

    public ImagenProductoController(ImagenProductoService imagenService, UsuarioService usuarioService) {
        this.imagenService = imagenService;
        this.usuarioService = usuarioService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImagenProductoResponseDTO> cargar(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long productoId,
            @RequestParam("archivo") MultipartFile archivo, Principal principal) {
        Usuario usuario = usuarioService.obtenerAutenticado(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(imagenService.guardar(productoId, archivo, usuario.getId()));
    }

    @GetMapping
    public ResponseEntity<List<ImagenProductoResponseDTO>> listar(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long productoId) {
        return ResponseEntity.ok(imagenService.listar(productoId));
    }

    @GetMapping("/{imagenId}")
    public ResponseEntity<byte[]> consultar(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long productoId,
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long imagenId) {
        ImagenProducto imagen = imagenService.obtener(productoId, imagenId);
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(imagen.getNombreArchivo(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(imagen.getTipoContenido()))
                .contentLength(imagen.getTamanio()).header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(imagen.getDatos());
    }

    @DeleteMapping("/{imagenId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long productoId,
            @PathVariable @Positive(message = "El identificador debe ser mayor a cero") Long imagenId,
            Principal principal) {
        Usuario usuario = usuarioService.obtenerAutenticado(principal);
        imagenService.eliminar(productoId, imagenId, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
