package com.uade.marketplace.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.marketplace.model.Producto;
import com.uade.marketplace.model.Rol;
import com.uade.marketplace.model.Sexo;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.ProductoRepository;
import com.uade.marketplace.repository.UsuarioRepository;
import com.uade.marketplace.security.JwtService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImagenProductoControllerIntegrationTest {

    private static final byte[] PNG = new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0x01, 0x02};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Usuario vendedor;
    private Producto producto;

    @BeforeEach
    void setUp() {
        vendedor = crearUsuario("vendedor_imagen", "imagen@test.com");
        producto = productoRepository
                .save(new Producto(null, "Notebook", "Notebook usada", 500000.0, 1, vendedor, null));
    }

    @Test
    void cargarListarYConsultarImagen() throws Exception {
        Long imagenId = cargarImagen(producto, vendedor);

        mockMvc.perform(get("/api/productos/{productoId}/imagenes", producto.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(imagenId))
                .andExpect(jsonPath("$[0].nombreArchivo").value("foto.png"))
                .andExpect(jsonPath("$[0].tipoContenido").value("image/png"))
                .andExpect(jsonPath("$[0].url").value("/api/productos/" + producto.getId() + "/imagenes/" + imagenId));

        mockMvc.perform(get("/api/productos/{productoId}/imagenes/{imagenId}", producto.getId(), imagenId))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG)).andExpect(header()
                        .string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("foto.png")))
                .andExpect(content().bytes(PNG));
    }

    @Test
    void cargarImagenSinAutenticacionDevuelve401() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.png", "image/png", PNG);

        mockMvc.perform(multipart("/api/productos/{productoId}/imagenes", producto.getId()).file(archivo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void otroUsuarioNoPuedeCargarNiEliminarImagen() throws Exception {
        Usuario otro = crearUsuario("otro_imagen", "otro.imagen@test.com");
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.png", "image/png", PNG);

        mockMvc.perform(multipart("/api/productos/{productoId}/imagenes", producto.getId()).file(archivo)
                .header(HttpHeaders.AUTHORIZATION, bearer(otro))).andExpect(status().isForbidden());

        Long imagenId = cargarImagen(producto, vendedor);
        mockMvc.perform(delete("/api/productos/{productoId}/imagenes/{imagenId}", producto.getId(), imagenId)
                .header(HttpHeaders.AUTHORIZATION, bearer(otro))).andExpect(status().isForbidden());
    }

    @Test
    void rechazaFormatoNoPermitidoYContenidoFalso() throws Exception {
        MockMultipartFile texto = new MockMultipartFile("archivo", "foto.txt", "text/plain", "hola".getBytes());
        mockMvc.perform(multipart("/api/productos/{productoId}/imagenes", producto.getId()).file(texto)
                .header(HttpHeaders.AUTHORIZATION, bearer(vendedor))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El formato debe ser JPEG, PNG o WebP"));

        MockMultipartFile pngFalso = new MockMultipartFile("archivo", "foto.png", "image/png",
                "esto no es una imagen".getBytes());
        mockMvc.perform(multipart("/api/productos/{productoId}/imagenes", producto.getId()).file(pngFalso)
                .header(HttpHeaders.AUTHORIZATION, bearer(vendedor))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El contenido del archivo no coincide con su formato"));
    }

    @Test
    void propietarioPuedeEliminarImagen() throws Exception {
        Long imagenId = cargarImagen(producto, vendedor);

        mockMvc.perform(delete("/api/productos/{productoId}/imagenes/{imagenId}", producto.getId(), imagenId)
                .header(HttpHeaders.AUTHORIZATION, bearer(vendedor))).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/productos/{productoId}/imagenes/{imagenId}", producto.getId(), imagenId))
                .andExpect(status().isNotFound());
    }

    private Long cargarImagen(Producto producto, Usuario usuario) throws Exception {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.png", "image/png", PNG);
        String respuesta = mockMvc
                .perform(multipart("/api/productos/{productoId}/imagenes", producto.getId()).file(archivo)
                        .header(HttpHeaders.AUTHORIZATION, bearer(usuario)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return Long.valueOf(respuesta.replaceAll(".*\\\"id\\\":(\\d+).*", "$1"));
    }

    private Usuario crearUsuario(String nombre, String mail) {
        return usuarioRepository.save(new Usuario(null, nombre, mail, "clave123", "Juan", "Perez",
                LocalDate.of(1995, 5, 20), Sexo.MASCULINO, Rol.USUARIO));
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.generarToken(usuario.getMail());
    }
}
