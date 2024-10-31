# CHALLENGE - Literalura

<p align="center">
    <img src="https://github.com/migmm/alura_challenge-desafio_1-logica/blob/main/assets/aluraoracle.png" alt="Logo"/>
</p>

Literalura es aplicación de consola Java Spring Boot que se integra con la API de Gutendex para buscar, almacenar y gestionar información de libros y autores.

## Características

- Búsqueda de libros por título a través de la API Gutendex
- Listado de libros registrados en la base de datos
- Listado de autores registrados y sus obras
- Búsqueda de autores por año específico (vivos durante ese año)
- Filtrado de libros por idioma
- Gestión automática de autores y vinculación con libros
- Persistencia de datos usando JPA/Hibernate

## Detalles Técnicos

### Uso del Cliente HTTP
La aplicación utiliza el Cliente HTTP nativo de Java (`java.net.http`) en `GutendexClient.java`:
- `HttpClient`: Crea conexiones HTTP
- `HttpRequest`: Construye peticiones GET a la API Gutendex
- `HttpResponse`: Maneja las respuestas de la API y las convierte a strings

### Validaciones

#### Validación de Entrada
- Validación de opciones del menú: Verifica entrada de números válidos
- Validación del formato de año (4 dígitos): `yearInput.matches("\\d{4}")`
- Validación de selección de idioma: Asegura que la selección esté dentro de las opciones disponibles

#### Validación de Procesamiento de Datos
- Validación de longitud del título del libro: Trunca a 254 caracteres
- Validación de longitud del nombre del autor: Trunca a 254 caracteres
- Verificación de libros duplicados: Verifica si el libro ya existe antes de guardar
- Verificación de disponibilidad de idioma: Asegura que el idioma existe en la base de datos antes de filtrar

#### Verificaciones de Nulos
- Validación de autor: Crea "Autor Desconocido" si no hay datos de autor disponibles
- Validación de idioma: Establece "Desconocido" si no se proporciona datos de idioma
- Validación de lista de libros: Inicializa ArrayList vacío si la colección de libros es nula

## Estructura del Proyecto

### DTOs (Objetos de Transferencia de Datos)
- `AuthorDTO.java`: Datos de autor desde la API Gutendex
- `BookDTO.java`: Datos de libro desde la API Gutendex
- `GutendexResponseDTO.java`: Estructura de respuesta de la API

### Modelos
- `Author.java`: Entidad de autor con anotaciones JPA
- `Book.java`: Entidad de libro con anotaciones JPA

### Repositorios
- `AuthorRepository.java`: Repositorio JPA para la entidad Autor
- `BookRepository.java`: Repositorio JPA para la entidad Libro

### Servicios
- `AuthorService.java`: Lógica de negocio para operaciones de autor
- `BookService.java`: Lógica de negocio para operaciones de libro
- `GutendexClient.java`: Servicio de integración con la API

### Interfaz de Usuario
- `ConsoleUI.java`: Implementación de la interfaz de consola

## Esquema de Base de Datos

### Tabla Autores
- id (Long, PK)
- name (String)
- birth_year (Integer)
- death_year (Integer)

### Tabla Libros
- id (Long, PK)
- title (String)
- language (String)
- download_count (Integer)
- author_id (Long, FK)

## Comenzando

1. Cloná el repositorio
2. Configurá la conexión a tu base de datos en `application.properties`
3. Creá una base de datos postgres con el nombre literalura
4. Ejecutá la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

## Opciones del Menú

1. Buscar libro por título
2. Listar libros registrados
3. Listar autores registrados
4. Listar autores vivos en un año específico
5. Listar libros por idioma
0. Salir

## Manejo de Errores

La aplicación incluye un manejo completo de errores para:
- Entrada de usuario inválida
- Problemas de conexión con la API
- Operaciones de base de datos
- Errores de procesamiento de datos

## Dependencias

- Spring Boot
- Spring Data JPA
- Jackson (procesamiento JSON)
- MySQL (base de datos)
