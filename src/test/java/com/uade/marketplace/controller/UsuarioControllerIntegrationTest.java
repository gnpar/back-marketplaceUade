package com.uade.marketplace.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Crea un usuario directo en la BD con la contrasena ya hasheada, tal como
    // quedaria luego de un registro real.
    private Usuario crearUsuarioEnBD(String nombreUsuario, String mail) {
        Usuario usuario = new Usuario(null, nombreUsuario, mail, passwordEncoder.encode("clave123"), "Juan", "Perez",
                LocalDate.of(1995, 5, 20), Sexo.MASCULINO);
        return usuarioRepository.save(usuario);
    }

    @Test
    void registrarUsuarioValido() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"Clave1234",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"1998-03-15","sexo":"FEMENINO"}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombreUsuario").value("ana123"))
                .andExpect(jsonPath("$.mail").value("ana@test.com"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1998-03-15"))
                .andExpect(jsonPath("$.sexo").value("FEMENINO"));

        assertFalse(usuarioRepository.findByMail("ana@test.com").isEmpty());
    }

    // La contrasena nunca se persiste en texto plano: se guarda su hash BCrypt.
    @Test
    void contrasenaSeGuardaHasheadaYNoEnTextoPlano() throws Exception {
        String contrasenaPlana = "Clave1234";

        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"Clave1234",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"1998-03-15","sexo":"FEMENINO"}
                """)).andExpect(status().isCreated());

        Usuario guardado = usuarioRepository.findByMail("ana@test.com").orElseThrow();
        assertNotEquals(contrasenaPlana, guardado.getContrasena());
        assertTrue(guardado.getContrasena().startsWith("$2"));
        assertTrue(passwordEncoder.matches(contrasenaPlana, guardado.getContrasena()));
    }

    // Registro + login end to end: la contrasena hasheada valida contra el texto
    // plano enviado al iniciar sesion.
    @Test
    void loginFuncionaLuegoDelRegistroConHash() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"Clave1234",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"1998-03-15","sexo":"FEMENINO"}
                """)).andExpect(status().isCreated());

        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"mail\":\"ana@test.com\",\"contrasena\":\"Clave1234\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.mail").value("ana@test.com"));
    }

    @Test
    void registrarUsuarioSinFechaNacimiento() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"Clave1234",
                 "nombre":"Ana","apellido":"Gomez","sexo":"FEMENINO"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuarioConFechaNacimientoFutura() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"Clave1234",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"2099-01-01","sexo":"FEMENINO"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuarioSinSexo() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"Clave1234",
                 "nombre":"Ana","apellido":"Gomez","fechaNacimiento":"1998-03-15"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuarioSinContrasena() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"1998-03-15","sexo":"FEMENINO"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuarioConMailInvalido() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"no-es-un-mail","contrasena":"Clave1234",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"1998-03-15","sexo":"FEMENINO"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuarioConContrasenaCorta() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"Abc1",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"1998-03-15","sexo":"FEMENINO"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuarioConContrasenaSinNumero() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"sololetras",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"1998-03-15","sexo":"FEMENINO"}
                """)).andExpect(status().isBadRequest());
    }

    @Test
    void registrarUsuarioConMailDuplicado() throws Exception {
        crearUsuarioEnBD("juan1", "juan@test.com");

        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"otro","mail":"juan@test.com","contrasena":"Clave1234",
                 "nombre":"Otro","apellido":"Usuario",
                 "fechaNacimiento":"1998-03-15","sexo":"OTRO"}
                """)).andExpect(status().isConflict());
    }

    @Test
    void registrarUsuarioConNombreUsuarioDuplicado() throws Exception {
        crearUsuarioEnBD("juan1", "juan@test.com");

        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"juan1","mail":"otro@test.com","contrasena":"Clave1234",
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
                .content("{\"mail\":\"noexiste@test.com\",\"contrasena\":\"clave123\"}"))
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
                {"nombreUsuario":"usuarioX","mail":"x@test.com",
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