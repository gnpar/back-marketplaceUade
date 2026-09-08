package com.uade.marketplace.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.marketplace.model.Producto;
import com.uade.marketplace.model.Sexo;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.CarritoItemRepository;
import com.uade.marketplace.repository.ProductoRepository;
import com.uade.marketplace.repository.UsuarioRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CarritoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    private Usuario usuario;

    private Producto producto;

    @BeforeEach
    void setUp() {
        usuario = usuarioRepository.save(new Usuario(null, "juanperez", "juan@test.com", "clave123", "Juan", "Perez",
                LocalDate.of(2000, 1, 1), Sexo.MASCULINO));
        producto = productoRepository.save(new Producto(null, "Mouse", "Mouse inalámbrico", 15000.00, null, null));
    }

    @Test
    void carritoVacioDevuelveListaVacia() throws Exception {
        mockMvc.perform(get("/api/carrito/{usuarioId}", usuario.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void agregarItemCreaItem() throws Exception {
        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON).content(
                "{\"usuarioId\":" + usuario.getId() + ",\"productoId\":" + producto.getId() + ",\"cantidad\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
                .andExpect(jsonPath("$.productoId").value(producto.getId()))
                .andExpect(jsonPath("$.nombreProducto").value("Mouse")).andExpect(jsonPath("$.precio").value(15000.00))
                .andExpect(jsonPath("$.cantidad").value(2));
    }

    @Test
    void agregarItemSinCantidadUsaUno() throws Exception {
        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":" + usuario.getId() + ",\"productoId\":" + producto.getId() + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cantidad").value(1));
    }

    @Test
    void agregarItemExistenteIncrementaCantidad() throws Exception {
        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON).content(
                "{\"usuarioId\":" + usuario.getId() + ",\"productoId\":" + producto.getId() + ",\"cantidad\":2}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON).content(
                "{\"usuarioId\":" + usuario.getId() + ",\"productoId\":" + producto.getId() + ",\"cantidad\":3}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.cantidad").value(5));

        mockMvc.perform(get("/api/carrito/{usuarioId}", usuario.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void agregarItemConUsuarioInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":999,\"productoId\":" + producto.getId() + ",\"cantidad\":1}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Usuario con ID 999 no encontrado"));
    }

    @Test
    void agregarItemConProductoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":" + usuario.getId() + ",\"productoId\":999,\"cantidad\":1}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Producto con ID 999 no encontrado"));
    }

    @Test
    void agregarItemConCantidadInvalidaDevuelve400() throws Exception {
        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON).content(
                "{\"usuarioId\":" + usuario.getId() + ",\"productoId\":" + producto.getId() + ",\"cantidad\":0}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("La cantidad debe ser mayor a 0"));
    }

    @Test
    void listarCarritoConItems() throws Exception {
        Producto otroProducto = productoRepository
                .save(new Producto(null, "Teclado", "Teclado mecánico", 45000.00, null, null));

        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON).content(
                "{\"usuarioId\":" + usuario.getId() + ",\"productoId\":" + producto.getId() + ",\"cantidad\":1}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON).content(
                "{\"usuarioId\":" + usuario.getId() + ",\"productoId\":" + otroProducto.getId() + ",\"cantidad\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/carrito/{usuarioId}", usuario.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombreProducto").value("Mouse"))
                .andExpect(jsonPath("$[1].nombreProducto").value("Teclado"));
    }

    @Test
    void quitarItemExistente() throws Exception {
        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON).content(
                "{\"usuarioId\":" + usuario.getId() + ",\"productoId\":" + producto.getId() + ",\"cantidad\":1}"))
                .andExpect(status().isOk());

        Long itemId = carritoItemRepository.findByUsuarioId(usuario.getId()).get(0).getId();

        mockMvc.perform(delete("/api/carrito/{id}", itemId)).andExpect(status().isNoContent());

        assertThat(carritoItemRepository.existsById(itemId)).isFalse();
    }

    @Test
    void quitarItemInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/api/carrito/{id}", 999L)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Item del carrito con ID 999 no encontrado"));
    }
}