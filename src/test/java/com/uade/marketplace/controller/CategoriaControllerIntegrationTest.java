package com.uade.marketplace.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.marketplace.model.Categoria;
import com.uade.marketplace.model.Rol;
import com.uade.marketplace.model.Sexo;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.CategoriaRepository;
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
class CategoriaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    // La gestion del catalogo de categorias es exclusiva del rol ADMIN.
    private String tokenAdmin;

    private String tokenUsuarioComun;

    @BeforeEach
    void setUp() {
        tokenAdmin = bearer(crearUsuario("admin1", "admin@test.com", Rol.ADMIN));
        tokenUsuarioComun = bearer(crearUsuario("juan1", "juan@test.com", Rol.USUARIO));
    }

    private Usuario crearUsuario(String nombreUsuario, String mail, Rol rol) {
        return usuarioRepository.save(new Usuario(null, nombreUsuario, mail, "clave123", "Juan", "Perez",
                LocalDate.of(1995, 5, 20), Sexo.MASCULINO, rol));
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.generarToken(usuario.getMail());
    }

    @Test
    void listarCategoriasOrdenadasAlfabeticamente() throws Exception {
        categoriaRepository.save(new Categoria(null, "Indumentaria"));
        categoriaRepository.save(new Categoria(null, "Electrónica"));
        categoriaRepository.save(new Categoria(null, "Hogar"));

        mockMvc.perform(get("/api/categorias").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3)).andExpect(jsonPath("$[0].nombre").value("Electrónica"))
                .andExpect(jsonPath("$[1].nombre").value("Hogar"))
                .andExpect(jsonPath("$[2].nombre").value("Indumentaria"));
    }

    @Test
    void crearCategoriaConNombreDuplicadoFalla() throws Exception {
        categoriaRepository.save(new Categoria(null, "Electrónica"));

        mockMvc.perform(post("/api/categorias").header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Electrónica\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarCategoriaSinCambiarNombreNoFalla() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria(null, "Electrónica"));

        mockMvc.perform(put("/api/categorias/{id}", categoria.getId()).header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Electrónica\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Electrónica"));
    }

    @Test
    void actualizarCategoriaConNombreDeOtraCategoriaFalla() throws Exception {
        categoriaRepository.save(new Categoria(null, "Electrónica"));
        Categoria hogar = categoriaRepository.save(new Categoria(null, "Hogar"));

        mockMvc.perform(put("/api/categorias/{id}", hogar.getId()).header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Electrónica\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarCategoriaExistente() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria(null, "Electrónica"));

        mockMvc.perform(delete("/api/categorias/{id}", categoria.getId()).header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isNoContent());

        assertFalse(categoriaRepository.existsById(categoria.getId()));
    }

    @Test
    void crearCategoriaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(
                post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Hogar\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void crearCategoriaConUsuarioComunDevuelve403() throws Exception {
        mockMvc.perform(post("/api/categorias").header(HttpHeaders.AUTHORIZATION, tokenUsuarioComun)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Hogar\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void crearCategoriaConAdmin() throws Exception {
        mockMvc.perform(post("/api/categorias").header(HttpHeaders.AUTHORIZATION, tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Hogar\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nombre").value("Hogar"));
    }

    @Test
    void listarCategoriasEsPublico() throws Exception {
        categoriaRepository.save(new Categoria(null, "Hogar"));

        mockMvc.perform(get("/api/categorias").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
