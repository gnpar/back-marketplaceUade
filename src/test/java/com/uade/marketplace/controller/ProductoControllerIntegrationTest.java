package com.uade.marketplace.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.marketplace.model.Categoria;
import com.uade.marketplace.model.Producto;
import com.uade.marketplace.repository.CategoriaRepository;
import com.uade.marketplace.repository.ProductoRepository;
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
class ProductoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Long categoriaId;

    @BeforeEach
    void crearCategoria() {
        categoriaId = categoriaRepository.save(new Categoria(null, "Informática")).getId();
    }

    @Test
    void listarProductosVacios() throws Exception {
        mockMvc.perform(get("/api/productos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void crearProducto() throws Exception {
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(
                "{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00,\"categoriaId\":"
                        + categoriaId + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Mouse"))
                .andExpect(jsonPath("$.descripcion").value("Mouse inalámbrico"))
                .andExpect(jsonPath("$.precio").value(15000.00))
                .andExpect(jsonPath("$.categoriaId").value(categoriaId));
    }

    @Test
    void listarProductosDespuesDeCrearUno() throws Exception {
        Producto producto = new Producto(null, "Teclado", "Teclado mecánico RGB", 45000.00, 10, null, null);
        productoRepository.save(producto);

        mockMvc.perform(get("/api/productos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Teclado"));
    }

    @Test
    void obtenerProductoPorIdExistente() throws Exception {
        Producto producto = new Producto(null, "Monitor", "Monitor 27 pulgadas", 350000.00, 10, null, null);
        Producto guardado = productoRepository.save(producto);

        mockMvc.perform(get("/api/productos/{id}", guardado.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Monitor")).andExpect(jsonPath("$.precio").value(350000.00));
    }

    @Test
    void obtenerProductoPorIdInexistente() throws Exception {
        mockMvc.perform(get("/api/productos/{id}", 999L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarProductoExistente() throws Exception {
        Producto producto = new Producto(null, "Monitor", "Monitor 27 pulgadas", 350000.00, 10, null, null);
        Producto guardado = productoRepository.save(producto);

        mockMvc.perform(put("/api/productos/{id}", guardado.getId()).contentType(MediaType.APPLICATION_JSON).content(
                "{\"nombre\":\"Monitor 4K\",\"descripcion\":\"Monitor 32 pulgadas 4K\",\"precio\":450000.00,\"categoriaId\":"
                        + categoriaId + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Monitor 4K"))
                .andExpect(jsonPath("$.descripcion").value("Monitor 32 pulgadas 4K"))
                .andExpect(jsonPath("$.precio").value(450000.00))
                .andExpect(jsonPath("$.categoriaId").value(categoriaId));
    }

    @Test
    void actualizarProductoInexistente() throws Exception {
        mockMvc.perform(put("/api/productos/{id}", 999L).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarProductoExistente() throws Exception {
        Producto producto = new Producto(null, "Teclado", "Teclado mecánico RGB", 45000.00, 10, null, null);
        Producto guardado = productoRepository.save(producto);

        mockMvc.perform(delete("/api/productos/{id}", guardado.getId())).andExpect(status().isNoContent());

        assertFalse(productoRepository.existsById(guardado.getId()));
    }

    @Test
    void eliminarProductoInexistente() throws Exception {
        mockMvc.perform(delete("/api/productos/{id}", 999L)).andExpect(status().isNotFound());
    }

    @Test
    void listarProductosOrdenadosAlfabeticamente() throws Exception {
        productoRepository.save(new Producto(null, "Zapatillas", "Zapatillas deportivas", 50000.00, 5, null, null));
        productoRepository.save(new Producto(null, "Auriculares", "Auriculares inalámbricos", 20000.00, 5, null, null));
        productoRepository.save(new Producto(null, "Mouse", "Mouse inalámbrico", 15000.00, 5, null, null));

        mockMvc.perform(get("/api/productos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)).andExpect(jsonPath("$[0].nombre").value("Auriculares"))
                .andExpect(jsonPath("$[1].nombre").value("Mouse"))
                .andExpect(jsonPath("$[2].nombre").value("Zapatillas"));
    }

    @Test
    void filtrarProductosPorCategoria() throws Exception {
        Long otraCategoriaId = categoriaRepository.save(new Categoria(null, "Indumentaria")).getId();

        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(
                "{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00,\"categoriaId\":"
                        + categoriaId + "}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(
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
    void crearProductoConCategoriaInexistente() throws Exception {
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(
                "{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00,\"categoriaId\":999}"))
                .andExpect(status().isNotFound());
    }
}
