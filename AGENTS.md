# AGENTS.md — ecommerce-g4

## Qué es esto

Proyecto e-commerce en Java 17 / Spring Boot 4.1.0 (proyecto universitario, UADE grupo 4). Proyecto Maven de módulo único, no es un monorepo.

## Comandos rápidos

```bash
./mvnw compile            # Compilar
./mvnw test               # Ejecutar todos los tests
./mvnw spring-boot:run     # Ejecutar la app (puerto 8080)
./mvnw clean package -DskipTests  # Build completo
```

No hay pipeline de CI/CD.

## Formatting y linting

Se usa **Spotless** con Eclipse JDT Formatter (configurado en `eclipse-formatter.xml`). El hook de pre-commit se instala automáticamente en el primer `mvn compile`.

```bash
./mvnw spotless:check   # Verificar formato (sin modificar archivos)
./mvnw spotless:apply   # Auto-formatear todos los .java
```

El formatter corre en la fase `verify` del build. Si el código no está formateado, `./mvnw verify` falla.

## Arquitectura

Spring Boot en capas: `Controller -> Service -> Repository -> Entity`

```
src/main/java/com/uade/e_commerce/
├── ECommerceApplication.java          # Punto de entrada
├── controller/ProductoController.java # API REST
├── service/ProductoService.java       # Lógica de negocio (@Transactional)
├── repository/ProductoRepository.java # Spring Data JPA
└── model/Producto.java                # Entidad JPA
```

El nombre del paquete usa guión bajo: `com.uade.e_commerce` (no `com.uade.e-commerce`).

## Base de datos

- Por defecto: H2 en memoria (`jdbc:h2:mem:testdb`). Los datos se pierden al reiniciar.
- Consola H2: `http://localhost:8080/h2-console`
- El conector MySQL está en el classpath (runtime) pero no está configurado — listo para producción.

## Generación de código

Se usa Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`). El procesamiento de anotaciones está configurado tanto en Maven como en Eclipse.

## API

Ruta base: `http://localhost:8080/api/productos`

- `GET /api/productos` — listar todos
- `GET /api/productos/{id}` — obtener por ID
- `POST /api/productos` — crear

Nota: `requests.http` usa `/api/users` que está desactualizado — el endpoint real es `/api/productos`.

## Testing

Un solo smoke test en `src/test/java/com/uade/e_commerce/ECommerceApplicationTests.java` (`contextLoads()`). Usa Spring Boot Test con JUnit 5.

Ejecutar tests: `./mvnw test`

## Cuidados

- El proyecto apunta a Java 17, pero puede ejecutarse en versiones más nuevas (probado con Java 25).
- El `launch.json` de VS Code referencia `${workspaceFolder}/.env` pero no hay archivo `.env` commiteado.
- `.gitignore` lista `mvnw`/`mvnw.cmd` pero están tracked forzadamente en git — no eliminarlos.
