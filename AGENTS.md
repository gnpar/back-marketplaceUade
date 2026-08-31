# AGENTS.md — marketplaceUade

## Qué es esto

Proyecto e-commerce (marketplaceUade) en Java 17 / Spring Boot 4.1.0 (proyecto universitario, UADE grupo 4). Proyecto Maven de módulo único, no es un monorepo.

## Comandos rápidos

```bash
./mvnw compile            # Compilar
./mvnw test               # Ejecutar todos los tests
./mvnw spring-boot:run     # Ejecutar la app (puerto 8080)
./mvnw clean package -DskipTests  # Build completo
```

No hay pipeline de CI/CD.

## Formatting y linting

Se usa **Spotless** con Eclipse JDT Formatter (configurado en `eclipse-formatter.xml`). El hook de pre-commit se instala automáticamente en el primer `mvn initialize` (o `mvn compile`).

```bash
./mvnw spotless:check   # Verificar formato (sin modificar archivos)
./mvnw spotless:apply   # Auto-formatear todos los .java
```

El formatter corre en la fase `verify` del build. Si el código no está formateado, `./mvnw verify` falla.

## Arquitectura

Spring Boot en capas: `Controller -> Service -> Repository -> Entity`

```
src/main/java/com/uade/marketplace/
├── MarketplaceUadeApplication.java    # Punto de entrada
├── controller/ProductoController.java # API REST
├── service/ProductoService.java       # Lógica de negocio (@Transactional)
├── repository/ProductoRepository.java # Spring Data JPA
└── model/Producto.java                # Entidad JPA
```

El nombre del paquete es `com.uade.marketplace`.

## Base de datos

- Por defecto: H2 en memoria (`jdbc:h2:mem:testdb`). Los datos se pierden al reiniciar.
- Consola H2: `http://localhost:8080/h2-console`
- MySQL (perfil `mysql`): config en `application-mysql.properties`, que lee variables de `.env.dev` (gitignored) vía `spring.config.import`. `MYSQL_ROOT_PASSWORD` es obligatoria; `MYSQL_HOST`, `DB_NAME`, `DB_USER` tienen defaults (`localhost`, `marketplace`, `root`). `createDatabaseIfNotExist=true` crea la BD automáticamente.
- `docker compose up -d` levanta MySQL (`3306`) y Adminer (`http://localhost:30080`). Particularidades locales del entorno se manejan en `compose.override.yaml` (gitignored, auto-fusionado).
- Ejecutar la app con MySQL: `./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql`

## Generación de código

Se usa Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`). El procesamiento de anotaciones está configurado tanto en Maven como en Eclipse.

## Testing

Smoke test (`MarketplaceUadeApplicationTests`) y tests de integración de la API (`controller/ProductoControllerIntegrationTest`). Usan Spring Boot Test con JUnit 5 y H2.

Ejecutar tests: `./mvnw test`

## Cuidados

- El proyecto apunta a Java 17, pero puede ejecutarse en versiones más nuevas (probado con Java 25).
