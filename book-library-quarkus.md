# Book Library — Guía de aprendizaje Quarkus REST

Documento de referencia construido durante el desarrollo paso a paso del proyecto `book-library-quarkus`. Incluye comparaciones con Spring Boot, preguntas, respuestas y observaciones del proceso de aprendizaje.

---

## Tabla de comparación rápida

| Concepto | Quarkus | Spring Boot |
|---|---|---|
| Entidad | `PanacheEntity` | `@Entity` + `@Repository` |
| Queries | Métodos estáticos en la entidad | Métodos en el `@Repository` |
| Bean de servicio | `@ApplicationScoped` | `@Service` / `@Component` |
| Inyección de dependencias | `@Inject` (Jakarta CDI) | `@Autowired` (Spring) |
| Controller | `@Path` + `@GET/@POST...` | `@RestController` + `@GetMapping...` |
| Parámetros de ruta | `@PathParam` | `@PathVariable` |
| Parámetros de query | `@QueryParam` + `@DefaultValue` | `@RequestParam` |
| Errores globales | `@Provider` + `ExceptionMapper<T>` | `@ControllerAdvice` + `@ExceptionHandler` |
| Transacciones | `@Transactional` (Jakarta) | `@Transactional` (Spring) |
| Configuración | `quarkus.*` en `application.properties` | `spring.*` en `application.properties` |
| Evento de arranque | `@Observes StartupEvent` | `@EventListener(ApplicationReadyEvent.class)` |
| Panel de administración | Dev UI (`/q/dev`) solo en Dev Mode | Actuator (`/actuator`) |
| Swagger UI | `/q/swagger-ui` | `/swagger-ui.html` |

---

## Estructura del proyecto

```
book-library-quarkus/
├── pom.xml
├── src/main/java/com/booklibrary/
│   ├── entity/
│   │   ├── Author.java
│   │   └── Book.java
│   ├── dto/
│   │   ├── AuthorDTO.java        (Request / Response / Summary)
│   │   ├── BookDTO.java          (Request / Response)
│   │   └── PagedResponse.java    (genérico <T>)
│   ├── mapper/
│   │   ├── AuthorMapper.java
│   │   └── BookMapper.java
│   ├── service/
│   │   ├── AuthorService.java
│   │   └── BookService.java
│   ├── resource/
│   │   ├── AuthorResource.java
│   │   └── BookResource.java
│   └── exception/
│       ├── BookLibraryException.java
│       └── GlobalExceptionMapper.java
└── src/main/resources/
    └── application.properties
```

---

## Dependencias (`pom.xml`)

```xml
<artifactId>quarkus-rest</artifactId>
<artifactId>quarkus-rest-jackson</artifactId>
<artifactId>quarkus-hibernate-orm-panache</artifactId>
<artifactId>quarkus-jdbc-postgresql</artifactId>
<artifactId>quarkus-hibernate-validator</artifactId>
<artifactId>quarkus-smallrye-openapi</artifactId>
```

---

## `application.properties`

```properties
# Base de datos
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=booklibrary
quarkus.datasource.password=booklibrary
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5435/booklibrary

# Hibernate
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.log.sql=true

# Puerto
quarkus.http.port=8080

# Swagger UI
quarkus.swagger-ui.always-include=true
```

### ¿Qué hace `drop-and-create`?
Cada vez que arranca la app borra todas las tablas y las vuelve a crear desde cero. Útil en desarrollo porque si cambias una entidad no tienes que migrar nada manualmente. En producción sería un desastre — perderías todos los datos. Ahí usarías `validate` o herramientas como Flyway/Liquibase.

---

## Entidades con Panache

### Patrón ActiveRecord

En Spring Boot el patrón típico separa la entidad del repositorio:

```java
// Entidad solo con campos
@Entity class Author { ... }

// Repository separado
@Repository interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByNationality(String nationality);
}
```

Con Panache, **la entidad es el repositorio**:

```java
@Entity
public class Author extends PanacheEntity {
    public String name;

    // Los queries van directo en la entidad
    public static List<Author> findByNationality(String nationality) {
        return list("nationality", nationality);
    }
}

// En el service, sin inyectar nada:
List<Author> authors = Author.findByNationality("Colombian");
```

### Observaciones al escribir `Author.java`

**¿Por qué no hay `id`?**
`PanacheEntity` ya incluye el `id` como `Long` autogenerado. Equivale a esto en Spring Boot:
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

**¿Por qué los campos son `public` y no `private` con getters/setters?**
Panache genera los accessors automáticamente en bytecode. Puedes escribir `author.name` directamente sin `author.getName()`. Menos boilerplate. En Spring Boot también funcionaría, solo que la convención allá es `private` + getters.

### `@NotBlank` vs `@Column(nullable = false)` — ¿hacen lo mismo?

No. Son dos niveles de validación diferentes:

- `@Column(nullable = false)` — validación a nivel de **base de datos**. No permite `NULL` en la columna SQL. Si insertas un `null` directamente en la BD, PostgreSQL lo rechaza.
- `@NotBlank` — validación a nivel de **aplicación**, antes de tocar la BD. Rechaza `null` AND strings vacíos (`""`) AND strings con solo espacios (`"   "`).

Se complementan: `@NotBlank` atrapa el problema temprano con un mensaje claro al cliente, `@Column(nullable = false)` es la última línea de defensa en la BD. En producción quieres las dos.

### ¿Qué significa `?1` en los queries de Panache?

Es el **primer parámetro** que le pasas al método (posicional). Si tuvieras dos parámetros:

```java
return list("LOWER(name) LIKE ?1 AND nationality = ?2", "%" + name + "%", nationality);
```

Similar a `@Query` en Spring Boot:
```java
@Query("SELECT b FROM Book b WHERE b.genre = ?1")
```

### `Author.java` completo

```java
@Entity
@Table(name = "authors")
public class Author extends PanacheEntity {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    public String name;

    @Size(max = 100)
    @Column(length = 100)
    public String nationality;

    @Size(max = 500)
    @Column(length = 500)
    public String biography;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Book> books = new ArrayList<>();

    public static List<Author> findByNameContaining(String name) {
        return list("LOWER(name) LIKE LOWER(?1)", "%" + name + "%");
    }

    public static List<Author> findByNationality(String nationality) {
        return list("nationality", nationality);
    }

    public static boolean existsByName(String name) {
        return count("LOWER(name) = LOWER(?1)", name) > 0;
    }
}
```

---

## Relaciones entre entidades

### `FetchType.LAZY` vs `FetchType.EAGER` — problema N+1

Con `EAGER` cada vez que cargas un Author automáticamente lanza otro query para traer todos sus libros, aunque no los necesites. Si tienes 100 autores en una lista serían 101 queries — 1 para los autores + 100 para los libros de cada uno. Eso es el problema N+1.

Con `LAZY` los libros no se cargan hasta que explícitamente los accedas. Si solo necesitas el nombre del autor, cero queries extra.

```java
// En Book.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "author_id", nullable = false)
@NotNull(message = "El autor es obligatorio")
public Author author;

// En Author.java
@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
public List<Book> books = new ArrayList<>();
```

### ¿Para qué sirve `unique = true` en el ISBN?

A nivel de base de datos PostgreSQL crea un índice único en esa columna. Si intentas insertar dos libros con el mismo ISBN, la BD lo rechaza. Se complementa con la validación de negocio en el Service.

---

## DTOs

### ¿Por qué tres clases en `AuthorDTO`?

- `Request` — lo que **recibe** el API cuando creas o actualizas un autor (POST/PUT)
- `Response` — lo que **devuelve** el API con todos los datos incluyendo `bookCount`
- `Summary` — versión reducida para cuando `Book` necesita mostrar su autor. Solo `id`, `name`, `nationality`. Evita el loop infinito de serialización JSON

### ¿Por qué `BookDTO.Response` usa `AuthorDTO.Summary` y no `AuthorDTO.Response`?

Cuando devuelves un libro no necesitas saber cuántos libros tiene ese autor (`bookCount`) ni su biografía completa. Solo necesitas identificarlo. `Summary` tiene lo mínimo.

### ¿Por qué no hay getters/setters ni Lombok en los DTOs?

Jackson puede serializar/deserializar campos `public` directamente sin getters. No es específico de Quarkus — en Spring Boot también funcionaría. La convención allá es `private` + getters, pero no es obligatorio.

### ¿Se podrían usar Records?

Sí, especialmente para los Response DTOs:
```java
public record Response(Long id, String title, String isbn, AuthorDTO.Summary author) {}
```
El problema es que los Records son inmutables. Para `Request` con validaciones puede complicarse. Para proyectos reales los Records son excelente opción para los Response DTOs.

### `PagedResponse<T>` — genérico

El `<T>` permite reutilizar la clase para cualquier tipo: `PagedResponse<AuthorDTO.Response>`, `PagedResponse<BookDTO.Response>`, etc. Sin el genérico tendrías que crear una clase diferente para cada tipo.

```java
public class PagedResponse<T> {
    public List<T> content;
    public int page;
    public int size;
    public long totalElements;
    public int totalPages;
    public boolean first;
    public boolean last;

    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        PagedResponse<T> response = new PagedResponse<>();
        response.content = content;
        response.page = page;
        response.size = size;
        response.totalElements = totalElements;
        response.totalPages = (int) Math.ceil((double) totalElements / size);
        response.first = page == 0;
        response.last = page >= response.totalPages - 1;
        return response;
    }
}
```

---

## Excepciones personalizadas

### Estructura

```java
public class BookLibraryException {

    public static class NotFoundException extends RuntimeException {
        public static NotFoundException book(Long id) {
            return new NotFoundException("Libro con id " + id + " no encontrado");
        }
        public static NotFoundException author(Long id) {
            return new NotFoundException("Autor con id " + id + " no encontrado");
        }
    }

    public static class ConflictException extends RuntimeException {
        public static ConflictException duplicateIsbn(String isbn) {
            return new ConflictException("Ya existe un libro con ISBN: " + isbn);
        }
    }

    public static class BadRequestException extends RuntimeException { ... }
}
```

> **Nota sobre clases anidadas estáticas:** Es una decisión de diseño del proyecto, no un patrón específico de Quarkus. Agrupa excepciones relacionadas en un solo archivo. En proyectos grandes es más común tener un archivo por excepción. La ventaja de agruparlas es que `BookLibraryException.` con autocompletado muestra todas disponibles.

---

## Manejo global de errores

### Spring Boot vs Quarkus

```java
// Spring Boot
@ControllerAdvice
public class GlobalHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handle(NotFoundException ex) {
        return ResponseEntity.status(404)...;
    }
}

// Quarkus / JAX-RS
@Provider
public static class NotFoundMapper
        implements ExceptionMapper<BookLibraryException.NotFoundException> {
    @Override
    public Response toResponse(BookLibraryException.NotFoundException ex) {
        return Response.status(404).entity(...).build();
    }
}
```

### ¿Qué hace `@Provider`?

Le dice a Quarkus "este componente es un proveedor JAX-RS, regístralo automáticamente". Sin esa anotación Quarkus no sabe que existe el mapper y las excepciones no serían interceptadas. Similar a `@Component` en Spring — sin él, Spring tampoco registra el bean.

### ¿Por qué cada mapper implementa `ExceptionMapper<T>` con un tipo diferente?

Porque cada `ExceptionMapper<T>` solo sabe manejar un tipo específico de excepción. El `<T>` le dice a Quarkus cuándo activarse:

- `ExceptionMapper<NotFoundException>` → atrapa solo `NotFoundException` → devuelve 404
- `ExceptionMapper<ConflictException>` → atrapa solo `ConflictException` → devuelve 409

### Formato de error uniforme

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe un libro con ISBN: 9780060883287",
  "timestamp": "2026-05-14T08:57:56.926",
  "violations": null
}
```

Para errores de validación, `violations` se llena con los mensajes:
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "La petición contiene campos inválidos",
  "timestamp": "2026-05-14T09:05:29.376",
  "violations": [
    "El nombre es obligatorio",
    "el tamaño debe estar entre 2 y 100"
  ]
}
```

> **Nota:** Quarkus tiene su propio manejador de `ConstraintViolationException` por defecto con un formato diferente. Para usar nuestro formato hay que agregar explícitamente un `ExceptionMapper<ConstraintViolationException>`.

---

## Mappers (entidad ↔ DTO)

### `@ApplicationScoped` e `@Inject`

```java
// Quarkus / CDI
@ApplicationScoped          // equivale a @Service / @Component en Spring
public class BookMapper {

    @Inject                 // equivale a @Autowired en Spring
    AuthorMapper authorMapper;
}
```

`@Inject` es el estándar de **Jakarta CDI**, no es específico de Quarkus. `@Autowired` es propio de Spring. En la práctica hacen lo mismo.

### ¿Por qué `updateEntity` recibe la entidad existente en vez de crear una nueva?

Si en un PUT crearas un objeto nuevo perderías:
- La lista de libros asociados
- El `id` original
- Cualquier dato que no venga en el Request

Con `updateEntity` tomas el objeto que ya existe en la BD y solo pisas los campos que el cliente envió. Hibernate detecta los cambios y hace el `UPDATE` automáticamente.

---

## Services

### ¿Por qué los métodos de lectura no tienen `@Transactional`?

Panache **requiere un contexto transaccional activo** para operaciones de escritura (`persist()`, `delete()`). Si intentas escribir sin `@Transactional` Quarkus lanza error en runtime. Las lecturas no requieren transacción aunque pueden beneficiarse de una.

### Dirty checking — ¿por qué no llamamos `persist()` en `update`?

Cuando cargas una entidad dentro de una transacción activa, Hibernate la "vigila". Al final de la transacción compara el estado actual del objeto con el estado original y si detecta cambios genera el `UPDATE` automáticamente.

Es como si Hibernate tomara una foto del objeto al cargarlo, y al cerrar la transacción compara esa foto con el estado actual. Si algo cambió, hace el UPDATE solo.

### ¿Por qué los queries personalizados van en la entidad y no en el Service?

Porque con Panache la entidad es responsable de su propia persistencia. En Spring Boot ibas al `@Repository`. En Panache vas directo a la entidad:

```java
// En Book.java — no en BookService.java
public static List<Book> findByAuthor(Long authorId) {
    return list("author.id", authorId);
}

public static boolean existsByIsbn(String isbn) {
    return count("isbn", isbn) > 0;
}

// Este método recibe el id del libro actual para evitar detectar
// su propio ISBN como duplicado al editar
public static boolean existsByIsbnAndIdNot(String isbn, Long id) {
    return count("isbn = ?1 AND id != ?2", isbn, id) > 0;
}
```

---

## Resources (Controllers)

### Diferencias con Spring Boot

```java
// Spring Boot
@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {

    @Autowired
    AuthorService authorService;

    @GetMapping
    public ResponseEntity<PagedResponse<AuthorDTO.Response>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(authorService.findAll(page, size));
    }

    @PostMapping
    public ResponseEntity<AuthorDTO.Response> create(@RequestBody @Valid AuthorDTO.Request request) {
        AuthorDTO.Response created = authorService.create(request);
        return ResponseEntity.created(uri).body(created);
    }
}

// Quarkus / JAX-RS
@Path("/api/v1/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorResource {

    @Inject
    AuthorService authorService;

    @GET
    public PagedResponse<AuthorDTO.Response> findAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return authorService.findAll(page, size);
    }

    @POST
    public Response create(@Valid AuthorDTO.Request request) {
        AuthorDTO.Response created = authorService.create(request);
        return Response.created(URI.create("/api/v1/authors/" + created.id))
                .entity(created).build();
    }
}
```

### Status codes por defecto

Quarkus asume:
- Métodos que devuelven un objeto → **200**
- Métodos que devuelven `void` → **204**

Solo cuando quieres algo diferente usas `Response` explícitamente (como el 201 en `create`). En Spring tenías que declarar `ResponseEntity<T>` siempre para controlar el status.

### `BookResource` — escrito de forma independiente

El `BookResource` fue escrito de forma independiente como ejercicio. Observaciones del proceso:

- La estructura es idéntica a `AuthorResource` — mismas anotaciones, mismo patrón
- El endpoint de búsqueda por autor se definió como `@Path("/author/{authorId}")` con `@PathParam` para ser consistente con el resto del API

---

## Dev Mode y Dev UI

### Dev Mode

```bash
./mvnw quarkus:dev
```

- **Hot reload instantáneo** al guardar cualquier archivo Java — más rápido que Spring Boot DevTools porque recarga solo las clases que cambiaron
- **Dev UI** en `http://localhost:8080/q/dev` — panel de control visual
- Consola interactiva (presiona `h` para ver opciones)

### Dev UI vs Actuator

Dev UI es el equivalente visual de Spring Boot Actuator. La diferencia clave: **Dev UI solo existe en Dev Mode**, en producción desaparece automáticamente. Actuator en Spring Boot está disponible en todos los perfiles si no lo configuras explícitamente.

---

## Dev Services

Quarkus tiene una feature llamada **Dev Services**: cuando detecta que tienes `quarkus-jdbc-postgresql` en el `pom.xml` y **no tienes una URL de base de datos configurada**, automáticamente levanta un contenedor de PostgreSQL usando Testcontainers sin que hagas nada.

En este proyecto lo configuramos manualmente para entender qué está pasando por debajo. Dev Services es útil una vez que ya dominas Quarkus.

---

## Endpoints del API

### Authors `/api/v1/authors`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/authors?page=0&size=10` | Lista paginada |
| `GET` | `/api/v1/authors/{id}` | Buscar por ID |
| `GET` | `/api/v1/authors/search?name=...` | Buscar por nombre |
| `POST` | `/api/v1/authors` | Crear autor |
| `PUT` | `/api/v1/authors/{id}` | Actualizar autor |
| `DELETE` | `/api/v1/authors/{id}` | Eliminar autor |

### Books `/api/v1/books`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/books?page=0&size=10` | Lista paginada |
| `GET` | `/api/v1/books/{id}` | Buscar por ID |
| `GET` | `/api/v1/books/author/{authorId}` | Libros de un autor |
| `POST` | `/api/v1/books` | Crear libro |
| `PUT` | `/api/v1/books/{id}` | Actualizar libro |
| `DELETE` | `/api/v1/books/{id}` | Eliminar libro |

---

## Cómo correr el proyecto

```bash
# 1. Levantar PostgreSQL
docker run --name booklibrary-quarkus \
  -e POSTGRES_DB=booklibrary \
  -e POSTGRES_USER=booklibrary \
  -e POSTGRES_PASSWORD=booklibrary \
  -p 5435:5432 -d postgres:16

# 2. Dev Mode
./mvnw quarkus:dev

# 3. Swagger UI
# http://localhost:8080/q/swagger-ui

# 4. Dev UI
# http://localhost:8080/q/dev
```
