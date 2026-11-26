package com.uade.marketplace.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.marketplace.model.Rol;
import com.uade.marketplace.model.Sexo;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests de la sesion por cookie HttpOnly y de la proteccion CSRF: que el login
 * ponga la cookie con los atributos correctos y sin el token en el body, que la
 * cookie autentique, que las requests que modifican datos con la cookie pidan
 * el token CSRF y que el logout borre la cookie.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CookieSesionIntegrationTest {

    private static final String COOKIE_SESION = CookieSesionService.NOMBRE_COOKIE;
    private static final String COOKIE_CSRF = "XSRF-TOKEN";
    private static final String HEADER_CSRF = "X-XSRF-TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = usuarioRepository
                .save(new Usuario(null, "juan1", "juan@test.com", passwordEncoder.encode("Clave1234"), "Juan", "Perez",
                        LocalDate.of(1995, 5, 20), Sexo.MASCULINO, Rol.USUARIO));
    }

    @Test
    void elLoginPoneElTokenEnUnaCookieHttpOnlyYNoEnElBody() throws Exception {
        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"mail\":\"juan@test.com\",\"contrasena\":\"Clave1234\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.mail").value("juan@test.com"))
                .andExpect(jsonPath("$.token").doesNotExist()).andExpect(cookie().exists(COOKIE_SESION))
                .andExpect(cookie().httpOnly(COOKIE_SESION, true)).andExpect(cookie().secure(COOKIE_SESION, true))
                .andExpect(cookie().sameSite(COOKIE_SESION, "Strict")).andExpect(cookie().path(COOKIE_SESION, "/api"))
                .andExpect(cookie().maxAge(COOKIE_SESION, 86400));
    }

    @Test
    void laCookieDeSesionAutenticaYPermiteRecuperarElUsuario() throws Exception {
        mockMvc.perform(get("/api/usuarios/me").cookie(login()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mail").value("juan@test.com"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void sinCookieElEndpointMeDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void cookieAdulteradaDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios/me").cookie(new Cookie(COOKIE_SESION, "no.es.unjwt"))
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    // Si viene el header Authorization, se usa solo el header: un header que no
    // es Bearer no cae en la cookie (y por eso no se salta el control de CSRF).
    @Test
    void unHeaderAuthorizationQueNoEsBearerNoUsaLaCookie() throws Exception {
        mockMvc.perform(get("/api/usuarios/me").cookie(login()).header(HttpHeaders.AUTHORIZATION, "Basic abc")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    void cualquierRespuestaEntregaLaCookieCsrfLegiblePorJs() throws Exception {
        mockMvc.perform(get("/api/categorias").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(cookie().exists(COOKIE_CSRF)).andExpect(cookie().httpOnly(COOKIE_CSRF, false));
    }

    // Simula el ataque CSRF: el navegador manda la cookie de sesion solo, pero el
    // sitio atacante no puede leer XSRF-TOKEN para armar el header.
    @Test
    void modificarDatosConLaCookieSinTokenCsrfDevuelve403() throws Exception {
        mockMvc.perform(delete("/api/usuarios/" + usuario.getId()).cookie(login()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void modificarDatosConLaCookieYUnTokenCsrfQueNoCoincideDevuelve403() throws Exception {
        mockMvc.perform(delete("/api/usuarios/" + usuario.getId())
                .cookie(login(), new Cookie(COOKIE_CSRF, "token-del-navegador")).header(HEADER_CSRF, "otro-token")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    void modificarDatosConLaCookieYElTokenCsrfFunciona() throws Exception {
        mockMvc.perform(delete("/api/usuarios/" + usuario.getId())
                .cookie(login(), new Cookie(COOKIE_CSRF, "token-del-navegador"))
                .header(HEADER_CSRF, "token-del-navegador")).andExpect(status().isNoContent());
    }

    @Test
    void elLogoutBorraLaCookieDeSesion() throws Exception {
        mockMvc.perform(post("/api/usuarios/logout")).andExpect(status().isNoContent())
                .andExpect(cookie().exists(COOKIE_SESION)).andExpect(cookie().maxAge(COOKIE_SESION, 0))
                .andExpect(cookie().value(COOKIE_SESION, ""));
    }

    @Test
    void elLogoutConSesionTambienPideElTokenCsrf() throws Exception {
        mockMvc.perform(post("/api/usuarios/logout").cookie(login())).andExpect(status().isForbidden());
    }

    /** Hace login y devuelve la cookie de sesion que puso el back. */
    private Cookie login() throws Exception {
        Cookie cookie = mockMvc
                .perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mail\":\"juan@test.com\",\"contrasena\":\"Clave1234\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getCookie(COOKIE_SESION);
        assertNotNull(cookie);
        return cookie;
    }
}
