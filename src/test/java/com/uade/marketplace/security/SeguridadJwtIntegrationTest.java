package com.uade.marketplace.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.marketplace.model.Rol;
import com.uade.marketplace.model.Sexo;
import com.uade.marketplace.model.Usuario;
import com.uade.marketplace.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDate;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests del filtro de validacion del JWT y de los permisos por ruta: que pasa
 * con un token ausente, invalido, vencido o de un usuario que ya no existe, y
 * como se aplican los roles.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadJwtIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String secret;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = usuarioRepository
                .save(new Usuario(null, "juan1", "juan@test.com", passwordEncoder.encode("Clave1234"), "Juan", "Perez",
                        LocalDate.of(1995, 5, 20), Sexo.MASCULINO, Rol.USUARIO));
    }

    @Test
    void rutaPublicaNoNecesitaToken() throws Exception {
        mockMvc.perform(get("/api/productos").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void rutaProtegidaSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/carrito").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void rutaProtegidaConTokenValidoDevuelve200() throws Exception {
        mockMvc.perform(get("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer(usuario.getMail()))
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void headerSinPrefijoBearerDevuelve401() throws Exception {
        mockMvc.perform(
                get("/api/carrito").header(HttpHeaders.AUTHORIZATION, jwtService.generarToken(usuario.getMail()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenMalFormadoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/carrito").header(HttpHeaders.AUTHORIZATION, "Bearer esto-no-es-un-jwt")
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // Token con la estructura correcta pero firmado con otra clave: la firma no
    // verifica y no se autentica a nadie.
    @Test
    void tokenConFirmaInvalidaDevuelve401() throws Exception {
        String tokenFalso = Jwts.builder().setSubject(usuario.getMail()).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(Keys.hmacShaKeyFor("otra-clave-secreta-distinta-pero-igual-de-larga-123456".getBytes()),
                        SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/api/carrito").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFalso)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // Token bien firmado pero con el vencimiento en el pasado.
    @Test
    void tokenVencidoDevuelve401() throws Exception {
        long unaHora = 3600000L;
        String tokenVencido = Jwts.builder().setSubject(usuario.getMail())
                .setIssuedAt(new Date(System.currentTimeMillis() - 2 * unaHora))
                .setExpiration(new Date(System.currentTimeMillis() - unaHora))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256).compact();

        mockMvc.perform(get("/api/carrito").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenVencido)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // Token valido de un usuario que ya no esta en la base: el UserDetailsService
    // no lo encuentra y la request queda anonima.
    @Test
    void tokenDeUsuarioInexistenteDevuelve401() throws Exception {
        mockMvc.perform(get("/api/carrito").header(HttpHeaders.AUTHORIZATION, bearer("fantasma@test.com"))
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // Registro -> login -> uso del token devuelto en una ruta protegida.
    @Test
    void elTokenQueDevuelveElLoginSirveParaLasRutasProtegidas() throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombreUsuario":"ana123","mail":"ana@test.com","contrasena":"Clave1234",
                 "nombre":"Ana","apellido":"Gomez",
                 "fechaNacimiento":"1998-03-15","sexo":"FEMENINO"}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.rol").value("USUARIO"));

        String respuesta = mockMvc
                .perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mail\":\"ana@test.com\",\"contrasena\":\"Clave1234\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String token = respuesta.replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/carrito").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    private String bearer(String mail) {
        return "Bearer " + jwtService.generarToken(mail);
    }
}
