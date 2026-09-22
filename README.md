# marketplaceUade — Marketplace de productos usados

Marketplace de compra-venta de productos usados entre estudiantes de UADE (proyecto universitario, Aplicaciones Interactivas, grupo 4 mie-noche online). Cualquier usuario registrado puede publicar productos usados para vender y otros usuarios pueden comprarlos.

Al comprar se reduce el stock disponible; los productos sin stock se pausan: dejan de aparecer en los listados y no pueden comprarse. Cuando un comprador inicia una compra, puede retomarla desde cualquier parte de la aplicación, similar al funcionamiento de un carrito en un sitio de ecommerce tradicional.

## Aspectos técnicos

- **Backend**: Java 17, Spring Boot 4.1.0, Spring Data JPA/Hibernate, Lombok, Maven.
- **Base de datos**: MySQL (relacional, conector incluido). El perfil por defecto es `mysql` (`spring.profiles.default`): cualquier forma de iniciar la app (Maven, IDE, dashboard, jar) usa MySQL y crea la BD y las tablas automáticamente. Los tests corren sobre el perfil `h2` (H2 en memoria), activado explícitamente en el build.
- **API**: REST. En una próxima etapa será consumida por un frontend en React.
- **Arquitectura en capas** (`com.uade.marketplace`):
  - `controller` — `@RestController`
  - `service` — `@Service` + `@Transactional` (lógica de negocio)
  - `repository` — `@Repository` extendiendo `JpaRepository` (acceso a datos)
  - `model` — entidades JPA (`@Entity`, `@Id`, `@GeneratedValue`, `@Column`, relaciones)
  - `dto` — `DTOs` para desacoplar las entidades de controller y service

### Modelo de datos

| Entidad | Atributos | Relaciones |
| --- | --- | --- |
| `Usuario` | nombreUsuario, mail, contraseña, nombre, apellido | 1:N con `Producto` |
| `Producto` | nombre, descripcion, precio, stock, imagenes, estado (activo/pausado) | N:1 con `Usuario` (vendedor), N:M con `Categoria` |
| `Categoria` | nombre | N:M con `Producto` (tabla `producto_categoria`) |
| `Compra` | usuario comprador, fecha, estado (en curso/cerrada) | 1:N con items de compra |
| `DetalleCompra` | producto, cantidad, precio unitario | N:1 con `Producto` y `Compra` |

## Entorno de desarrollo

### 0. Prerrequisitos

Java 17, Docker y Maven (o el wrapper `./mvnw`).

### 1. Crear `.env.dev`

Crear un archivo `.env.dev` en la raíz del proyecto. Solo `MYSQL_ROOT_PASSWORD` es obligatoria:

```
MYSQL_ROOT_PASSWORD=mi_password
```

`MYSQL_HOST`, `DB_NAME` y `DB_USER` tienen defaults (`localhost`, `marketplace`, `root`).

### 2. Iniciar la base de datos

```bash
docker compose up -d
```

Levanta MySQL (`localhost:3306`) y Adminer en `http://localhost:30080`.

### 3. Iniciar la aplicación

```bash
./mvnw spring-boot:run
```

Inicia la app en `http://localhost:8080` con MySQL y crea la BD y las tablas automáticamente.

Para correr sin MySQL (H2 en memoria):

```bash
./mvnw spring-boot:run -Dapp.profiles=h2
```

### 4. Ejecutar los tests

```bash
./mvnw test
```

Los tests corren sobre H2 (perfil `h2`, activado por el build), no requieren MySQL.

### Formateo

```bash
./mvnw spotless:apply   # Formatear código (Eclipse JDT, se verifica en `verify`)
```

## Casos de uso

### Gestión de usuarios

- **Registro**: solicita nombre de usuario, mail, contraseña, nombre y apellido.
- **Login**: identifica al usuario mediante mail y contraseña y devuelve un JWT con vencimiento, que el cliente manda en el header `Authorization` de las rutas protegidas (ver [Autenticación y permisos](#autenticación-y-permisos-jwt)).

### Catálogo de productos

- Tras autenticarse, la home muestra dos secciones: productos ordenados alfabéticamente y las categorías del sitio.
- Al seleccionar un producto se accede a su detalle: imagen ampliada y descripción.
- Desde el detalle se agrega el producto a la compra en curso. Si no tiene stock, se visualiza esa situación y no puede agregarse.

### Compra (en lugar de carrito)

No hay carrito: un usuario inicia una compra, navega por otros productos y puede retomarla desde cualquier parte de la aplicación.

### Gestión de productos (vendedor)

- **Alta**: publicación con una o más fotos, descripción y categoría.
- **Stock**: el vendedor maneja el stock de sus productos.
- **Baja**: el vendedor puede eliminar sus productos.
- Al comprarse, se reduce la cantidad disponible. Sin cantidad disponible, el producto se pausa: no aparece en listados ni puede comprarse.

## API

Base: `http://localhost:8080/api/productos`

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/productos` | Listar todos los productos. Filtros opcionales: `?categoriaId=` y/o `?usuarioId=` (vendedor) |
| GET | `/api/productos/{id}` | Obtener producto por ID |
| POST | `/api/productos` | Crear producto. Requiere usuario autenticado: queda registrado como vendedor |
| PUT | `/api/productos/{id}` | Actualizar producto (404 si no existe; 403 si no lo creó el usuario autenticado) |
| DELETE | `/api/productos/{id}` | Eliminar producto (204 si se elimina; 403 si no lo creó el usuario autenticado; 404 si no existe) |

### Imágenes de productos

Cada producto admite hasta 5 imágenes de 5 MB cada una. Se aceptan archivos JPEG, PNG y WebP; además del
`Content-Type`, se valida la firma binaria para impedir que un archivo de otro tipo se presente como imagen. Las
imágenes se almacenan en la base de datos y se eliminan junto con el producto.

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/productos/{productoId}/imagenes` | Cargar una imagen como `multipart/form-data`, campo `archivo` (solo el vendedor) |
| GET | `/api/productos/{productoId}/imagenes` | Listar metadatos y URL de las imágenes (público) |
| GET | `/api/productos/{productoId}/imagenes/{imagenId}` | Consultar el archivo de imagen (público) |
| DELETE | `/api/productos/{productoId}/imagenes/{imagenId}` | Eliminar una imagen (solo el vendedor) |

## Autenticación y permisos (JWT)

La API es *stateless*: no hay sesión ni cookies. El login (`POST /api/usuarios/login`) devuelve un JWT y,
a partir de ahí, cada request lo manda en el header:

```
Authorization: Bearer <token>
```

**Contrato del token**: firmado con HS256 y la clave `jwt.secret`, `sub` = mail del usuario, `exp` = emisión +
`jwt.expiration` ms (24 hs por defecto). El rol **no** viaja en el token: se lee de la base de datos al
validarlo, así un cambio de rol tiene efecto sin esperar a que el token venza.

**Cómo se valida** (`com.uade.marketplace.security`):

1. `JwtAuthenticationFilter` (un `OncePerRequestFilter`) lee el header, valida firma y vencimiento con
   `JwtService` y, si está todo bien, carga al usuario con `UsuarioDetailsService` (`UserDetailsService`) y lo
   deja autenticado en el `SecurityContext` de esa request.
2. Si no hay token, está vencido, la firma no verifica o el usuario ya no existe, la request sigue anónima: el
   filtro nunca responde el error, lo decide la cadena de Spring Security según la ruta.
3. `SecurityConfig` define los permisos por ruta; los errores salen con el mismo formato JSON que el resto de la
   API (`401 Usuario no autenticado`, `403 No tenés permisos para acceder a este recurso`).

En los controllers el usuario autenticado se resuelve desde el `Principal`, cuyo nombre es el mail.

### Permisos por ruta

| Ruta | Permiso |
| --- | --- |
| `POST /api/usuarios/registro`, `POST /api/usuarios/login` | Público |
| `GET /api/productos/**`, `GET /api/categorias/**` | Público (catálogo) |
| `POST/PUT/DELETE /api/productos/**` | Autenticado (y ser el vendedor para editar/borrar) |
| `/api/carrito/**` | Autenticado |
| `GET /api/usuarios/{id}` | Autenticado |
| `PUT/DELETE /api/usuarios/{id}` | La propia cuenta, o rol `ADMIN` |
| `GET /api/usuarios` (listado completo) | Rol `ADMIN` |
| `POST/PUT/DELETE /api/categorias/**` | Rol `ADMIN` |
| `/actuator/**`, `/h2-console/**` | Público (herramientas de desarrollo) |

### Roles

`Usuario.rol` es un enum (`USUARIO` / `ADMIN`) que se traduce a la autoridad `ROLE_USUARIO` / `ROLE_ADMIN`.
El registro **siempre** crea usuarios con rol `USUARIO`: el rol no se puede mandar en el alta. Para tener un
administrador se promueve un usuario desde la base:

```sql
UPDATE usuarios SET rol = 'ADMIN' WHERE mail = 'admin@test.com';
```

## Validaciones y respuestas de error

Los controllers validan los DTO antes de ejecutar los servicios. Productos requieren nombre y descripcion
no vacios (hasta 255 caracteres), precio y stock no negativos, y una categoria con identificador positivo.
El stock debe enviarse tanto al crear como al actualizar. Categorias requieren un nombre no vacio de hasta
255 caracteres. El carrito requiere un producto con identificador positivo y una cantidad positiva si se
envia; al omitir la cantidad, sigue usando uno. El login requiere mail valido y contrasena no vacia.
Los identificadores de rutas y filtros, cuando se envian, deben ser positivos.

Los errores conservan el contrato `status`, `mensaje` y `timestamp` de `ErrorResponseDTO`:

| Codigo | Situacion |
| --- | --- |
| 400 | Campos invalidos, JSON mal formado, cuerpo ausente o parametros con tipos incorrectos |
| 401 / 403 | Autenticacion o permisos insuficientes; los filtros JWT mantienen sus handlers |
| 404 | Recurso inexistente |
| 405 / 415 | Metodo HTTP o tipo de contenido no soportado |
| 409 | Duplicados o conflictos de integridad con datos relacionados |
| 500 | Fallo inesperado: mensaje generico al cliente y detalle en el log del servidor |

`GlobalExceptionHandler` extiende `ResponseEntityExceptionHandler` para conservar los codigos y headers
HTTP de Spring y unificar el cuerpo de las respuestas. Las excepciones propias de cada categoria siguen
usando sus codigos. Las validaciones de campos indican el campo y el motivo en `mensaje`.

Pruebas nuevas: `ValidacionesIntegrationTest` y `GlobalExceptionHandlerTest`. Se ejecutan con el resto de
la suite mediante `./mvnw test` (en CMD: `mvnw.cmd test`). Verificar formato con `./mvnw spotless:check`.
