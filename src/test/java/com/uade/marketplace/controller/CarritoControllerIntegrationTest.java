package com.uade.marketplace.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.marketplace.model.CarritoItem;
import com.uade.marketplace.model.Producto;
import com.uade.marketplace.model.Rol;
import com.uade.marketplace.model.Sexo;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.CarritoItemRepository;
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
class CarritoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    private Usuario comprador;

    private Usuario vendedor;

    private Producto producto;

    @BeforeEach
    void setUp() {
        comprador = crearUsuarioEnBD("juanperez", "juan@test.com");
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
    void carritoVacioDevuelveListaVacia() throws Exception {
        mockMvc.perform(get("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void agregarItemCreaItem() throws Exception {
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":2}")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber()).andExpect(jsonPath("$.usuarioId").value(comprador.getId()))
                .andExpect(jsonPath("$.productoId").value(producto.getId()))
                .andExpect(jsonPath("$.nombreProducto").value("Mouse")).andExpect(jsonPath("$.precio").value(15000.00))
                .andExpect(jsonPath("$.cantidad").value(2));
    }

    @Test
    void agregarItemSinCantidadUsaUno() throws Exception {
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON).content("{\"productoId\":" + producto.getId() + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.cantidad").value(1));
    }

    @Test
    void agregarItemExistenteIncrementaCantidad() throws Exception {
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":2}")).andExpect(status().isCreated());

        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":3}")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidad").value(5));

        mockMvc.perform(get("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void agregarItemSinAutenticacionDevuelve401() throws Exception {
        mockMvc.perform(post("/api/carrito").contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":1}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void agregarItemConUsuarioInexistenteDevuelve401() throws Exception {
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearerDeMail("nadie@test.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":1}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void agregarItemConProductoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON).content("{\"productoId\":999,\"cantidad\":1}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Producto con ID 999 no encontrado"));
    }

    @Test
    void agregarItemConCantidadInvalidaDevuelve400() throws Exception {
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":0}")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("cantidad: La cantidad debe ser mayor a cero"));
    }

    @Test
    void agregarProductoPropioDevuelve400() throws Exception {
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(vendedor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":1}")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.mensaje")
                        .value("No podés comprar tu propio producto (ID " + producto.getId() + ")"));
    }

    @Test
    void listarCarritoConItems() throws Exception {
        Producto otroProducto = productoRepository
                .save(new Producto(null, "Teclado", "Teclado mecánico", 45000.00, 10, vendedor, null));

        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":1}")).andExpect(status().isCreated());
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + otroProducto.getId() + ",\"cantidad\":1}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].nombreProducto").value("Mouse"))
                .andExpect(jsonPath("$[1].nombreProducto").value("Teclado"));
    }

    @Test
    void quitarItemExistente() throws Exception {
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":1}")).andExpect(status().isCreated());

        Long itemId = carritoItemRepository.findByUsuarioId(comprador.getId()).get(0).getId();

        mockMvc.perform(
                delete("/api/carrito/items/{itemId}", itemId).header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isNoContent());

        assertThat(carritoItemRepository.existsById(itemId)).isFalse();
    }

    @Test
    void quitarItemInexistenteDevuelve404() throws Exception {
        mockMvc.perform(
                delete("/api/carrito/items/{itemId}", 999L).header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Item del carrito con ID 999 no encontrado"));
    }

    @Test
    void quitarItemDeOtroUsuarioDevuelve403() throws Exception {
        Usuario otro = crearUsuarioEnBD("otrousuario", "otro@test.com");

        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":1}")).andExpect(status().isCreated());

        Long itemId = carritoItemRepository.findByUsuarioId(comprador.getId()).get(0).getId();

        mockMvc.perform(delete("/api/carrito/items/{itemId}", itemId).header(HttpHeaders.AUTHORIZATION, bearer(otro)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));

        assertThat(carritoItemRepository.findById(itemId)).isPresent();
    }

    @Test
    void vaciarCarritoEliminaTodosLosItems() throws Exception {
        Producto otroProducto = productoRepository
                .save(new Producto(null, "Teclado", "Teclado mecánico", 45000.00, 10, vendedor, null));

        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":1}")).andExpect(status().isCreated());
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + otroProducto.getId() + ",\"cantidad\":2}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isNoContent());

        assertThat(carritoItemRepository.findByUsuarioId(comprador.getId())).isEmpty();
    }

    @Test
    void vaciarCarritoConUsuarioInexistenteDevuelve401() throws Exception {
        mockMvc.perform(delete("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearerDeMail("nadie@test.com")))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void checkoutCalculaTotalYDescuentaStock() throws Exception {
        // Agrego 2 unidades del mouse (precio 15000, stock inicial 10)
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + producto.getId() + ",\"cantidad\":2}")).andExpect(status().isCreated());

        // Checkout: total esperado = 15000 * 2 = 30000
        mockMvc.perform(post("/api/carrito/checkout").header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pedidoId").isNumber())
                .andExpect(jsonPath("$.usuarioId").value(comprador.getId())).andExpect(jsonPath("$.fecha").isNotEmpty())
                .andExpect(jsonPath("$.total").value(30000.00)).andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].nombreProducto").value("Mouse"))
                .andExpect(jsonPath("$.items[0].cantidad").value(2))
                .andExpect(jsonPath("$.items[0].precioUnitario").value(15000.00));

        // El stock debe haber bajado de 10 a 8
        assertThat(productoRepository.findById(producto.getId()).get().getStock()).isEqualTo(8);

        // El carrito debe quedar vacío
        assertThat(carritoItemRepository.findByUsuarioId(comprador.getId())).isEmpty();
    }

    @Test
    void checkoutConCarritoVacioDevuelve400() throws Exception {
        mockMvc.perform(post("/api/carrito/checkout").header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void checkoutConStockInsuficienteDevuelve400() throws Exception {
        // Producto con stock 1
        Producto pocoStock = productoRepository
                .save(new Producto(null, "Webcam", "Webcam HD", 20000.00, 1, vendedor, null));

        // Agrego 1 unidad al carrito (permitido, hay stock)
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + pocoStock.getId() + ",\"cantidad\":1}")).andExpect(status().isCreated());

        // Reduzco el stock del producto a 0 por fuera (simula que otro lo compró antes
        // del checkout)
        pocoStock.setStock(0);
        productoRepository.save(pocoStock);

        // Checkout debe fallar por falta de stock
        mockMvc.perform(post("/api/carrito/checkout").header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void checkoutConProductoPropioDevuelve400() throws Exception {
        // El comprador tiene en el carrito (cargado por fuera) un producto propio
        Producto productoPropio = productoRepository
                .save(new Producto(null, "MiTeclado", "Mi teclado", 45000.00, 5, comprador, null));
        carritoItemRepository.save(new CarritoItem(null, comprador, productoPropio, 1));

        mockMvc.perform(post("/api/carrito/checkout").header(HttpHeaders.AUTHORIZATION, bearer(comprador)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje")
                        .value("No podés comprar tu propio producto (ID " + productoPropio.getId() + ")"));
    }

    @Test
    void agregarItemSinStockSuficienteDevuelve400() throws Exception {
        // Producto con stock 2
        Producto pocoStock = productoRepository
                .save(new Producto(null, "Parlante", "Parlante bluetooth", 30000.00, 2, vendedor, null));

        // Intento agregar 5 unidades: no hay stock suficiente
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(comprador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":" + pocoStock.getId() + ",\"cantidad\":5}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
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