package com.alura.literalura.service;

import com.alura.literalura.dto.BookDTO;
import com.alura.literalura.dto.AuthorDTO;
import com.alura.literalura.model.Author;
import com.alura.literalura.model.Book;
import com.alura.literalura.repository.AuthorRepository;
import com.alura.literalura.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GutendexClient gutendexClient;

    public Book searchBooksByTitle(String title) {
        if (bookRepository.existsByTitle(title)) {
            throw new RuntimeException("El libro con el título '" + title + "' ya está registrado.");
        }

        try {
            List<BookDTO> bookDTOs = gutendexClient.searchBooksByTitle(title);
            System.out.println("=== Respuesta de Gutendex ===");
            if (!bookDTOs.isEmpty()) {
                BookDTO bookDTO = bookDTOs.get(0);
                System.out.println("Libro encontrado: " + bookDTO.getTitle());
                System.out.println("Autores encontrados: " + bookDTO.getAuthors().size());
                for (AuthorDTO authorDTO : bookDTO.getAuthors()) {
                    System.out.println("  Autor: " + authorDTO.getName());
                    System.out.println("  Año nacimiento: " + authorDTO.getBirthYear());
                    System.out.println("  Año muerte: " + authorDTO.getDeathYear());
                }
                return convertToBook(bookDTO);
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error al buscar libros: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error searching books by title", e);
        }
    }

    @Transactional
    private Book convertToBook(BookDTO bookDTO) {
        System.out.println("\n=== Iniciando conversión de BookDTO a Book ===");
        Book book = new Book();
        book.setTitle(bookDTO.getTitle().substring(0, Math.min(bookDTO.getTitle().length(), 254)));

        if (!bookDTO.getLanguages().isEmpty()) {
            book.setLanguage(bookDTO.getLanguages().get(0));
        } else {
            book.setLanguage("Unknown");
        }

        book.setDownloadCount(bookDTO.getDownload_count());

        if (!bookDTO.getAuthors().isEmpty()) {
            AuthorDTO authorDTO = bookDTO.getAuthors().get(0);
            System.out.println("\nProcesando autor:");
            System.out.println("  Nombre: " + authorDTO.getName());
            System.out.println("  Año nacimiento DTO: " + authorDTO.getBirthYear());
            System.out.println("  Año muerte DTO: " + authorDTO.getDeathYear());

            Author author = authorRepository.findFirstByName(authorDTO.getName());

            if (author == null) {
                System.out.println("  Creando nuevo autor");
                author = new Author();
                author.setName(authorDTO.getName().substring(0, Math.min(authorDTO.getName().length(), 254)));
                author.setBirthYear(authorDTO.getBirthYear());
                author.setDeathYear(authorDTO.getDeathYear());
                // Guardar el autor primero para obtener su ID
                author = authorRepository.save(author);
            } else {
                System.out.println("  Actualizando autor existente");
                author.setBirthYear(authorDTO.getBirthYear());
                author.setDeathYear(authorDTO.getDeathYear());
                // Actualizar el autor
                author = authorRepository.save(author);
            }

            // Establecer la relación bidireccional
            book.setAuthor(author);
            if (author.getBooks() == null) {
                author.setBooks(new ArrayList<>());
            }
            author.getBooks().add(book);

        } else {
            System.out.println("\nNo se encontraron autores, creando autor desconocido");
            Author unknownAuthor = authorRepository.findFirstByName("Unknown Author");
            if (unknownAuthor == null) {
                unknownAuthor = new Author();
                unknownAuthor.setName("Unknown Author");
                unknownAuthor.setBooks(new ArrayList<>());
                unknownAuthor = authorRepository.save(unknownAuthor);
            }
            book.setAuthor(unknownAuthor);
            unknownAuthor.getBooks().add(book);
        }

        // Guardar el libro después de establecer todas las relaciones
        return bookRepository.save(book);
    }

    @Transactional
    public void saveBook(Book book) {
        System.out.println("\n=== Guardando libro ===");
        if (book.getAuthor() != null) {
            System.out.println("Autor del libro a guardar:");
            System.out.println("  Nombre: " + book.getAuthor().getName());
            System.out.println("  Año nacimiento: " + book.getAuthor().getBirthYear());
            System.out.println("  Año muerte: " + book.getAuthor().getDeathYear());

            // Asegurarse de que la lista de libros del autor no sea null
            if (book.getAuthor().getBooks() == null) {
                book.getAuthor().setBooks(new ArrayList<>());
            }

            // Actualizar la relación bidireccional
            if (!book.getAuthor().getBooks().contains(book)) {
                book.getAuthor().getBooks().add(book);
            }

            // Cortar el nombre del autor si es necesario
            book.getAuthor().setName(book.getAuthor().getName().substring(0, Math.min(book.getAuthor().getName().length(), 254)));

            // Guardar primero el autor
            Author savedAuthor = authorRepository.save(book.getAuthor());
            book.setAuthor(savedAuthor);
        }

        // Guardar el libro
        bookRepository.save(book);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getBooksByLanguage(String language) {
        return bookRepository.findByLanguage(language);
    }

    public boolean isBookRegistered(Book book) {
        return bookRepository.existsByTitleAndAuthor(book.getTitle(), book.getAuthor());
    }

    public List<String> getAvailableLanguages() {
        return bookRepository.findAll().stream()
                .map(Book::getLanguage)
                .distinct()
                .collect(Collectors.toList());
    }
}