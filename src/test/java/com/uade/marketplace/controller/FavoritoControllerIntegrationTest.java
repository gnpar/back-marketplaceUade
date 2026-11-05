package com.uade.marketplace.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.marketplace.model.Producto;
import com.uade.marketplace.model.Rol;
import com.uade.marketplace.model.Sexo;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.FavoritoRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FavoritoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    private Usuario comprador;

    private Usuario otroUsuario;

    private Usuario vendedor;

    private Producto producto;

    @BeforeEach
    void setUp() {
        comprador = crearUsuarioEnBD("comprador1", "comprador@test.com");
        otroUsuario = crearUsuarioEnBD("otrousuario", "otro@test.com");
        vendedor = crearUsuarioEnBD("vendedor1", "vendedor@test.com");
        producto = productoRepository
                .save(new Producto(null, "Mouse", "Mouse inalámbrico", 15000.00, 10, vendedor, null));
    }

    // Crea un usuario directo en la BD (no pasa por el registro).
    private Usuario crearUsuarioEnBD(String nombreUsuario, String mail) {
        return usuarioRepository.save(new Usuario(null, nombreUsuario, mail, "clave123", "Juan", "Perez",
                LocalDate.of(2000, 1, 1), Sexo.MASCULINO, Rol.USUARIO));
    }

    @Test
    void favoritosVaciosDevuelveListaVacia() throws Exception {
        mockMvc.perform(get("/api/favoritos").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void agregarFavoritoDevuelveElProducto() throws Exception {
        mockMvc.perform(post("/api/favoritos/{productoId}", producto.getId()).header(HttpHeaders.AUTHORIZATION,
                bearer(comprador))).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(producto.getId()))
                .andExpect(jsonPath("$.nombre").value("Mouse")).andExpect(jsonPath("$.precio").value(15000.00))
                .andExpect(jsonPath("$.usuarioId").value(vendedor.getId()));

        assertThat(favoritoRepository.existsByUsuarioIdAndProductoId(comprador.getId(), producto.getId())).isTrue();
    }

    @Test
    void agregarFavoritoDosVecesNoDuplica() throws Exception {
        mockMvc.perform(post("/api/favoritos/{productoId}", producto.getId()).header(HttpHeaders.AUTHORIZATION,
                bearer(comprador))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/favoritos/{productoId}", producto.getId()).header(HttpHeaders.AUTHORIZATION,
                bearer(comprador))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/favoritos").header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listarSoloDevuelveLosFavoritosDelUsuario() throws Exception {
        mockMvc.perform(post("/api/favoritos/{productoId}", producto.getId()).header(HttpHeaders.AUTHORIZATION,
                bearer(comprador))).andExpect(status().isCreated());

        mockMvc.perform(get("/api/favoritos").header(HttpHeaders.AUTHORIZATION, bearer(otroUsuario)))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(get("/api/favoritos").header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(producto.getId()));
    }

    @Test
    void quitarFavoritoExistenteDevuelve204() throws Exception {
        mockMvc.perform(post("/api/favoritos/{productoId}", producto.getId()).header(HttpHeaders.AUTHORIZATION,
                bearer(comprador))).andExpect(status().isCreated());

        mockMvc.perform(delete("/api/favoritos/{productoId}", producto.getId()).header(HttpHeaders.AUTHORIZATION,
                bearer(comprador))).andExpect(status().isNoContent());

        assertThat(favoritoRepository.existsByUsuarioIdAndProductoId(comprador.getId(), producto.getId())).isFalse();
    }

    @Test
    void quitarFavoritoInexistenteDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/favoritos/{productoId}", producto.getId()).header(HttpHeaders.AUTHORIZATION,
                bearer(comprador))).andExpect(status().isNoContent());
    }

    @Test
    void favoritosSinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/favoritos")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/favoritos/{productoId}", producto.getId())).andExpect(status().isUnauthorized());
    }

    @Test
    void agregarFavoritoConProductoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/favoritos/{productoId}", 999L).header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Producto con ID 999 no encontrado"));
    }

    @Test
    void agregarFavoritoConIdInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/favoritos/{productoId}", 0L).header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void eliminarProductoBorraSusFavoritos() throws Exception {
        mockMvc.perform(post("/api/favoritos/{productoId}", producto.getId()).header(HttpHeaders.AUTHORIZATION,
                bearer(comprador))).andExpect(status().isCreated());

        mockMvc.perform(
                delete("/api/productos/{id}", producto.getId()).header(HttpHeaders.AUTHORIZATION, bearer(vendedor)))
                .andExpect(status().isNoContent());

        assertThat(favoritoRepository.findByUsuarioIdOrderByIdAsc(comprador.getId())).isEmpty();
    }

    @Test
    void eliminarUsuarioBorraSusFavoritos() throws Exception {
        mockMvc.perform(post("/api/favoritos/{productoId}", producto.getId()).header(HttpHeaders.AUTHORIZATION,
                bearer(comprador))).andExpect(status().isCreated());

        mockMvc.perform(
                delete("/api/usuarios/{id}", comprador.getId()).header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isNoContent());

        assertThat(favoritoRepository.findByUsuarioIdOrderByIdAsc(comprador.getId())).isEmpty();
    }

    // Todas las rutas protegidas se prueban con un JWT real: se emite con el
    // mismo JwtService de la app y viaja en el header Authorization, igual que
    // lo haria el frontend.
    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.generarToken(usuario.getMail());
    }

}
