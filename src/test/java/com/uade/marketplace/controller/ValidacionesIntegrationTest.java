package com.uade.marketplace.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ValidacionesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private JwtService jwtService;

    private String token;
    private Usuario vendedor;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        vendedor = usuarioRepository.save(new Usuario(null, "validaciones", "validaciones@test.com", "hash", "Ana",
                "Perez", LocalDate.of(1995, 1, 1), Sexo.FEMENINO, Rol.ADMIN));
        token = "Bearer " + jwtService.generarToken(vendedor.getMail());
        categoria = categoriaRepository.save(new Categoria(null, "Tecnologia"));
    }

    private Map<String, Object> productoValido() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Mouse");
        body.put("descripcion", "Mouse inalambrico");
        body.put("precio", 100.0);
        body.put("stock", 5);
        body.put("categoriaId", categoria.getId());
        return body;
    }

    static Stream<Arguments> camposInvalidos() {
        return Stream.of(Arguments.of("nombre", null), Arguments.of("nombre", "   "),
                Arguments.of("nombre", "a".repeat(256)), Arguments.of("descripcion", ""),
                Arguments.of("descripcion", "a".repeat(256)), Arguments.of("precio", null), Arguments.of("precio", -1),
                Arguments.of("stock", null), Arguments.of("stock", -1), Arguments.of("categoriaId", null),
                Arguments.of("categoriaId", 0));
    }

    @ParameterizedTest
    @MethodSource("camposInvalidos")
    void crearProductoInvalidoDevuelve400SinPersistir(String campo, Object valor) throws Exception {
        Map<String, Object> body = productoValido();
        body.put(campo, valor);
        long cantidadAnterior = productoRepository.count();
        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje", containsString(campo))).andExpect(jsonPath("$.timestamp").exists());
        assertEquals(cantidadAnterior, productoRepository.count());
    }

    @Test
    void actualizarProductoInvalidoNoModificaElStock() throws Exception {
        Producto producto = productoRepository
                .save(new Producto(null, "Mouse", "Descripcion", 100.0, 5, vendedor, categoria));
        Map<String, Object> body = productoValido();
        body.put("stock", -1);
        mockMvc.perform(put("/api/productos/{id}", producto.getId()).header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensaje", containsString("stock")));
        assertEquals(5, productoRepository.findById(producto.getId()).orElseThrow().getStock());
    }

    @Test
    void precioYStockCeroSonValidos() throws Exception {
        Map<String, Object> body = productoValido();
        body.put("precio", 0);
        body.put("stock", 0);
        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.stock").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"nombre\":null}", "{\"nombre\":\"   \"}"})
    void categoriaSinNombreDevuelve400EnAltaYActualizacion(String body) throws Exception {
        mockMvc.perform(post("/api/categorias").header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("nombre")));
        mockMvc.perform(put("/api/categorias/{id}", categoria.getId()).header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("nombre")));
        assertEquals("Tecnologia", categoriaRepository.findById(categoria.getId()).orElseThrow().getNombre());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"productoId\":0}", "{\"productoId\":-1}", "{\"productoId\":1,\"cantidad\":0}",
            "{\"productoId\":1,\"cantidad\":-1}"})
    void carritoInvalidoDevuelve400(String body) throws Exception {
        mockMvc.perform(post("/api/carrito").header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"mail\":\"invalido\",\"contrasena\":\"clave\"}",
            "{\"mail\":\"usuario@test.com\",\"contrasena\":\"   \"}"})
    void loginInvalidoDevuelve400(String body) throws Exception {
        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "{\"stock\":\"muchos\"}"})
    void cuerpoAusenteOJsonInvalidoDevuelve400(String body) throws Exception {
        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje", containsString("JSON valido")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/productos/abc", "/api/productos/0", "/api/categorias/-1",
            "/api/productos?categoriaId=abc", "/api/productos?categoriaId=0", "/api/productos?usuarioId=-1"})
    void parametrosInvalidosDevuelven400(String url) throws Exception {
        mockMvc.perform(get(url)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void tipoDeContenidoIncorrectoDevuelve415() throws Exception {
        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.TEXT_PLAIN).content("producto")).andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    void categoriaDuplicadaConserva409() throws Exception {
        mockMvc.perform(post("/api/categorias").header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Tecnologia\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
    }
}
