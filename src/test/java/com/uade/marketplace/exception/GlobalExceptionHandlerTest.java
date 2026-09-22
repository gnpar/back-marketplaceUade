package com.uade.marketplace.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void errorInesperadoNoExponeDetallesInternos() throws Exception {
        mockMvc.perform(get("/fallo")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensaje", not(containsString("detalle-interno"))))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void conflictoDePersistenciaDevuelve409SinExponerSql() throws Exception {
        mockMvc.perform(get("/conflicto")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje", not(containsString("SQL"))));
    }

    @Test
    void metodoIncorrectoConserva405YHeaderAllow() throws Exception {
        mockMvc.perform(post("/fallo")).andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(header().string(HttpHeaders.ALLOW, containsString("GET")));
    }

    @RestController
    static class ControladorDePrueba {
        @GetMapping("/fallo")
        public String fallo() {
            throw new IllegalStateException("detalle-interno");
        }

        @GetMapping("/conflicto")
        public String conflicto() {
            throw new DataIntegrityViolationException("SQL con detalles internos");
        }
    }
}
