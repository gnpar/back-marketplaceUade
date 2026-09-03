package com.uade.marketplace.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.marketplace.model.Sexo;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.UsuarioRepository;
import java.time.LocalDate;
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
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario crearUsuarioEnBD(String nombreUsuario, String mail) {
        Usuario usuario = new Usuario(null, nombreUsuario, mail, "clave123", "Juan", "Perez", LocalDate.of(1995, 5, 20),
                Sexo.MASCULINO);
        return usuarioRepository.save(usuario);
    }

    @Test
    void registrarUsuarioValido() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"1234",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"1998-03-15","sexo":"FEMENINO"}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombreUsuario").value("ana123"))
                .andExpect(jsonPath("$.mail").value("ana@test.com"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1998-03-15"))
                .andExpect(jsonPath("$.sexo").value("FEMENINO"));

        assertFalse(usuarioRepository.findByMail("ana@test.com").isEmpty());
    }

    @Test
    void registrarUsuarioSinFechaNacimiento() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"1234",
                 "nombre":"Ana","apellido":"Gomez","sexo":"FEMENINO"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuarioConFechaNacimientoFutura() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"1234",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"2099-01-01","sexo":"FEMENINO"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuarioSinSexo() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"1234",
                 "nombre":"Ana","apellido":"Gomez","fechaNacimiento":"1998-03-15"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuarioConMailDuplicado() throws Exception {
        crearUsuarioEnBD("juan1", "juan@test.com");

        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"otro","mail":"juan@test.com","contrasena":"1234",
                 "nombre":"Otro","apellido":"Usuario",
                 "fechaNacimiento":"1998-03-15","sexo":"OTRO"}
                """)).andExpect(status().isConflict());
    }

    @Test
    void registrarUsuarioConNombreUsuarioDuplicado() throws Exception {
        crearUsuarioEnBD("juan1", "juan@test.com");

        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"juan1","mail":"otro@test.com","contrasena":"1234",
                 "nombre":"Otro","apellido":"Usuario",
                 "fechaNacimiento":"1998-03-15","sexo":"OTRO"}
                """)).andExpect(status().isConflict());
    }

    @Test
    void loginConCredencialesCorrectas() throws Exception {
        crearUsuarioEnBD("juan1", "juan@test.com");

        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"mail\":\"juan@test.com\",\"contrasena\":\"clave123\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.mail").value("juan@test.com"));
    }

    @Test
    void loginConContrasenaIncorrecta() throws Exception {
        crearUsuarioEnBD("juan1", "juan@test.com");

        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"mail\":\"juan@test.com\",\"contrasena\":\"mala\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginConMailInexistente() throws Exception {
        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"mail\":\"noexiste@test.com\",\"contrasena\":\"1234\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerUsuarioPorIdExistente() throws Exception {
        Usuario guardado = crearUsuarioEnBD("juan1", "juan@test.com");

        mockMvc.perform(get("/api/usuarios/{id}", guardado.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombreUsuario").value("juan1"));
    }

    @Test
    void obtenerUsuarioPorIdInexistente() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 999L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarUsuarioExistente() throws Exception {
        Usuario guardado = crearUsuarioEnBD("juan1", "juan@test.com");

        mockMvc.perform(put("/api/usuarios/{id}", guardado.getId()).contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"juan1","mail":"juan@test.com",
                 "nombre":"Juan Actualizado","apellido":"Perez",
                 "fechaNacimiento":"1995-05-20","sexo":"MASCULINO"}
                """)).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Juan Actualizado"));
    }

    @Test
    void actualizarUsuarioInexistente() throws Exception {
        mockMvc.perform(put("/api/usuarios/{id}", 999L).contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"x","mail":"x@test.com",
                 "nombre":"X","apellido":"X",
                 "fechaNacimiento":"1995-05-20","sexo":"OTRO"}
                """)).andExpect(status().isNotFound());
    }

    @Test
    void eliminarUsuarioExistente() throws Exception {
        Usuario guardado = crearUsuarioEnBD("juan1", "juan@test.com");

        mockMvc.perform(delete("/api/usuarios/{id}", guardado.getId())).andExpect(status().isNoContent());

        assertFalse(usuarioRepository.existsById(guardado.getId()));
    }

    @Test
    void eliminarUsuarioInexistente() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 999L)).andExpect(status().isNotFound());
    }
}
