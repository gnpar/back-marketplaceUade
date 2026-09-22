package com.uade.marketplace.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.marketplace.model.Categoria;
import com.uade.marketplace.model.Producto;
import com.uade.marketplace.model.Rol;
import com.uade.marketplace.model.Sexo;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.CategoriaRepository;
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
class ProductoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long categoriaId;

    private Usuario vendedor;

    @BeforeEach
    void setUp() {
        categoriaId = categoriaRepository.save(new Categoria(null, "Informática")).getId();
        vendedor = crearUsuarioEnBD("vendedor1", "vendedor@test.com");
    }

    // Crea un usuario directo en la BD (no pasa por el registro).
    private Usuario crearUsuarioEnBD(String nombreUsuario, String mail) {
        return usuarioRepository.save(new Usuario(null, nombreUsuario, mail, "clave123", "Juan", "Perez",
                LocalDate.of(1995, 5, 20), Sexo.MASCULINO, Rol.USUARIO));
    }

    private Producto guardarProducto(String nombre, Usuario duenio) {
        return productoRepository
                .save(new Producto(null, nombre, "Descripción de " + nombre, 10000.00, 10, duenio, null));
    }

    @Test
    void listarProductosVacios() throws Exception {
        mockMvc.perform(get("/api/productos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void crearProductoSinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(
                "{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00,\"categoriaId\":"
                        + categoriaId + "}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void crearProductoAsignaElUsuarioAutenticadoComoVendedor() throws Exception {
        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))
                .contentType(MediaType.APPLICATION_JSON).content(
                        "{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00,\"categoriaId\":"
                                + categoriaId + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Mouse"))
                .andExpect(jsonPath("$.descripcion").value("Mouse inalámbrico"))
                .andExpect(jsonPath("$.precio").value(15000.00)).andExpect(jsonPath("$.categoriaId").value(categoriaId))
                .andExpect(jsonPath("$.usuarioId").value(vendedor.getId()));
    }

    @Test
    void crearProductoConUsuarioInexistenteDevuelve401() throws Exception {
        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, bearerDeMail("nadie@test.com"))
                .contentType(MediaType.APPLICATION_JSON).content(
                        "{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00,\"categoriaId\":"
                                + categoriaId + "}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void listarProductosDespuesDeCrearUno() throws Exception {
        productoRepository.save(new Producto(null, "Teclado", "Teclado mecánico RGB", 45000.00, 10, vendedor, null));

        mockMvc.perform(get("/api/productos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Teclado"))
                .andExpect(jsonPath("$[0].usuarioId").value(vendedor.getId()));
    }

    @Test
    void obtenerProductoPorIdExistente() throws Exception {
        Producto producto = guardarProducto("Monitor", vendedor);

        mockMvc.perform(get("/api/productos/{id}", producto.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(producto.getId()))
                .andExpect(jsonPath("$.nombre").value("Monitor")).andExpect(jsonPath("$.precio").value(10000.00))
                .andExpect(jsonPath("$.usuarioId").value(vendedor.getId()));
    }

    @Test
    void obtenerProductoPorIdInexistente() throws Exception {
        mockMvc.perform(get("/api/productos/{id}", 999L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarProductoPorSuVendedor() throws Exception {
        Producto producto = guardarProducto("Monitor", vendedor);

        mockMvc.perform(put("/api/productos/{id}", producto.getId()).header(HttpHeaders.AUTHORIZATION, bearer(vendedor))
                .contentType(MediaType.APPLICATION_JSON).content(
                        "{\"nombre\":\"Monitor 4K\",\"descripcion\":\"Monitor 32 pulgadas 4K\",\"precio\":450000.00,\"categoriaId\":"
                                + categoriaId + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(producto.getId()))
                .andExpect(jsonPath("$.nombre").value("Monitor 4K"))
                .andExpect(jsonPath("$.descripcion").value("Monitor 32 pulgadas 4K"))
                .andExpect(jsonPath("$.precio").value(450000.00))
                .andExpect(jsonPath("$.categoriaId").value(categoriaId))
                .andExpect(jsonPath("$.usuarioId").value(vendedor.getId()));
    }

    @Test
    void actualizarProductoDeOtroUsuarioDevuelve403() throws Exception {
        Producto producto = guardarProducto("Monitor", vendedor);
        Usuario otro = crearUsuarioEnBD("otro_vendedor", "otro@test.com");

        mockMvc.perform(put("/api/productos/{id}", producto.getId()).header(HttpHeaders.AUTHORIZATION, bearer(otro))
                .contentType(MediaType.APPLICATION_JSON).content(
                        "{\"nombre\":\"Monitor 4K\",\"descripcion\":\"Monitor 32 pulgadas 4K\",\"precio\":450000.00,\"categoriaId\":"
                                + categoriaId + "}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void actualizarProductoInexistente() throws Exception {
        mockMvc.perform(put("/api/productos/{id}", 999L).header(HttpHeaders.AUTHORIZATION, bearer(vendedor))
                .contentType(MediaType.APPLICATION_JSON).content(
                        "{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00,\"categoriaId\":"
                                + categoriaId + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarProductoPorSuVendedor() throws Exception {
        Producto producto = guardarProducto("Teclado", vendedor);

        mockMvc.perform(
                delete("/api/productos/{id}", producto.getId()).header(HttpHeaders.AUTHORIZATION, bearer(vendedor)))
                .andExpect(status().isNoContent());

        assertFalse(productoRepository.existsById(producto.getId()));
    }

    @Test
    void eliminarProductoDeOtroUsuarioDevuelve403() throws Exception {
        Producto producto = guardarProducto("Teclado", vendedor);
        Usuario otro = crearUsuarioEnBD("otro_vendedor", "otro@test.com");

        mockMvc.perform(delete("/api/productos/{id}", producto.getId()).header(HttpHeaders.AUTHORIZATION, bearer(otro)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));

        assertTrue(productoRepository.existsById(producto.getId()));
    }

    @Test
    void eliminarProductoInexistente() throws Exception {
        mockMvc.perform(delete("/api/productos/{id}", 999L).header(HttpHeaders.AUTHORIZATION, bearer(vendedor)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarProductosOrdenadosAlfabeticamente() throws Exception {
        productoRepository.save(new Producto(null, "Zapatillas", "Zapatillas deportivas", 50000.00, 5, vendedor, null));
        productoRepository
                .save(new Producto(null, "Auriculares", "Auriculares inalámbricos", 20000.00, 5, vendedor, null));
        productoRepository.save(new Producto(null, "Mouse", "Mouse inalámbrico", 15000.00, 5, vendedor, null));

        mockMvc.perform(get("/api/productos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)).andExpect(jsonPath("$[0].nombre").value("Auriculares"))
                .andExpect(jsonPath("$[1].nombre").value("Mouse"))
                .andExpect(jsonPath("$[2].nombre").value("Zapatillas"));
    }

    @Test
    void filtrarProductosPorCategoria() throws Exception {
        Long otraCategoriaId = categoriaRepository.save(new Categoria(null, "Indumentaria")).getId();

        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))
                .contentType(MediaType.APPLICATION_JSON).content(
                        "{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00,\"categoriaId\":"
                                + categoriaId + "}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))
                .contentType(MediaType.APPLICATION_JSON).content(
                        "{\"nombre\":\"Zapatillas\",\"descripcion\":\"Zapatillas deportivas\",\"precio\":50000.00,\"categoriaId\":"
                                + otraCategoriaId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(
                get("/api/productos").param("categoriaId", categoriaId.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Mouse"))
                .andExpect(jsonPath("$[0].categoriaId").value(categoriaId));
    }

    @Test
    void filtrarProductosPorCategoriaInexistente() throws Exception {
        mockMvc.perform(get("/api/productos").param("categoriaId", "999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtrarProductosPorVendedor() throws Exception {
        Usuario otro = crearUsuarioEnBD("otro_vendedor", "otro@test.com");
        productoRepository.save(new Producto(null, "Teclado", "Teclado mecánico", 45000.00, 10, vendedor, null));
        productoRepository.save(new Producto(null, "Mouse", "Mouse inalámbrico", 15000.00, 10, otro, null));

        mockMvc.perform(get("/api/productos").param("usuarioId", vendedor.getId().toString())
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].nombre").value("Teclado"))
                .andExpect(jsonPath("$[0].usuarioId").value(vendedor.getId()));
    }

    @Test
    void filtrarProductosPorVendedorInexistente() throws Exception {
        mockMvc.perform(get("/api/productos").param("usuarioId", "999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtrarProductosPorVendedorYCategoria() throws Exception {
        Long otraCategoriaId = categoriaRepository.save(new Categoria(null, "Indumentaria")).getId();
        productoRepository.save(new Producto(null, "Teclado", "Teclado mecánico", 45000.00, 10, vendedor,
                new Categoria(categoriaId, "Informática")));
        productoRepository.save(new Producto(null, "Zapatillas", "Zapatillas deportivas", 50000.00, 10, vendedor,
                new Categoria(otraCategoriaId, "Indumentaria")));

        mockMvc.perform(get("/api/productos").param("usuarioId", vendedor.getId().toString())
                .param("categoriaId", otraCategoriaId.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Zapatillas"));
    }

    @Test
    void crearProductoConCategoriaInexistente() throws Exception {
        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))
                .contentType(MediaType.APPLICATION_JSON).content(
                        "{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00,\"categoriaId\":999}"))
                .andExpect(status().isNotFound());
    }

    // Todas las rutas protegidas se prueban con un JWT real: se emite con el
    // mismo JwtService de la app y viaja en el header Authorization, igual que
    // lo haria el frontend.
    private String bearer(Usuario usuario) {
        return bearerDeMail(usuario.getMail());
    }

    private String bearerDeMail(String mail) {
        return "Bearer " + jwtService.generarToken(mail);
    }

}