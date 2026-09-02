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
- **Login**: identifica al usuario mediante mail y contraseña.

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
| GET | `/api/productos` | Listar todos los productos |
| GET | `/api/productos/{id}` | Obtener producto por ID |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Actualizar producto (404 si no existe) |
| DELETE | `/api/productos/{id}` | Eliminar producto (204 si se elimina, 404 si no existe) |
