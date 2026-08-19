package com.uade.e_commerce.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.e_commerce.model.Producto;
import com.uade.e_commerce.repository.ProductoRepository;
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

    @Test
    void listarProductosVacios() throws Exception {
        mockMvc.perform(get("/api/productos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void crearProducto() throws Exception {
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Mouse\",\"descripcion\":\"Mouse inalámbrico\",\"precio\":15000.00}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Mouse"))
                .andExpect(jsonPath("$.descripcion").value("Mouse inalámbrico"))
                .andExpect(jsonPath("$.precio").value(15000.00));
    }

    @Test
    void listarProductosDespuesDeCrearUno() throws Exception {
        Producto producto = new Producto(null, "Teclado", "Teclado mecánico RGB", 45000.00);
        productoRepository.save(producto);

        mockMvc.perform(get("/api/productos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Teclado"));
    }

    @Test
    void obtenerProductoPorIdExistente() throws Exception {
        Producto producto = new Producto(null, "Monitor", "Monitor 27 pulgadas", 350000.00);
        Producto guardado = productoRepository.save(producto);

        mockMvc.perform(get("/api/productos/{id}", guardado.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Monitor")).andExpect(jsonPath("$.precio").value(350000.00));
    }

    @Test
    void obtenerProductoPorIdInexistente() throws Exception {
        mockMvc.perform(get("/api/productos/{id}", 999L).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }
}
