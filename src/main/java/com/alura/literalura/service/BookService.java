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
        try {
            List<BookDTO> bookDTOs = gutendexClient.searchBooksByTitle(title);
            if (!bookDTOs.isEmpty()) {
                BookDTO bookDTO = bookDTOs.get(0);
                Book existingBook = findExistingBook(bookDTO);
                if (existingBook != null) {
                    System.out.println("El libro ya está registrado en la base de datos.");
                    return null;
                }

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

    private Book findExistingBook(BookDTO bookDTO) {
        if (bookDTO.getAuthors().isEmpty()) {
            return bookRepository.findByTitle(bookDTO.getTitle());
        }

        String authorName = bookDTO.getAuthors().get(0).getName();
        Author existingAuthor = authorRepository.findFirstByName(authorName);

        if (existingAuthor != null) {
            List<Book> existingBooks = bookRepository.findByTitleAndAuthor(bookDTO.getTitle(), existingAuthor);
            if (!existingBooks.isEmpty()) {
                return existingBooks.get(0);
            }
        }

        return null;
    }

    @Transactional
    private Book convertToBook(BookDTO bookDTO) {
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
                author = authorRepository.save(author);
            } else {
                System.out.println("  Actualizando autor existente");
                author.setBirthYear(authorDTO.getBirthYear());
                author.setDeathYear(authorDTO.getDeathYear());
                author = authorRepository.save(author);
            }

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

        return bookRepository.save(book);
    }

    @Transactional
    public void saveBook(Book book) {
        System.out.println("\n--- Guardando libro ---");
        if (book.getAuthor() != null) {
            System.out.println("Autor del libro a guardar:");
            System.out.println("  Nombre: " + book.getAuthor().getName());
            System.out.println("  Año nacimiento: " + book.getAuthor().getBirthYear());
            System.out.println("  Año muerte: " + book.getAuthor().getDeathYear());

            if (book.getAuthor().getBooks() == null) {
                book.getAuthor().setBooks(new ArrayList<>());
            }

            if (!book.getAuthor().getBooks().contains(book)) {
                book.getAuthor().getBooks().add(book);
            }

            book.getAuthor().setName(book.getAuthor().getName().substring(0, Math.min(book.getAuthor().getName().length(), 254)));
            Author savedAuthor = authorRepository.save(book.getAuthor());
            book.setAuthor(savedAuthor);
        }

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