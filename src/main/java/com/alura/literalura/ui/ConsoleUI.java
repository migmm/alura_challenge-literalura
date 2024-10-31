package com.alura.literalura.ui;

import com.alura.literalura.model.Author;
import com.alura.literalura.model.Book;
import com.alura.literalura.service.AuthorService;
import com.alura.literalura.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
public class ConsoleUI {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    private Scanner scanner = new Scanner(System.in);

    public void start() {
        procesaOpciones();
    }

    private void mostrarLogo() {
        System.out.println();
        System.out.println(" _        _   _                           _");
        System.out.println("| |      (_) | |                         | |");
        System.out.println("| |       _  | |_    ___   _ __    __ _  | |  _  _   _ __    __ _");
        System.out.println("| |      | | | __|  / _ \\ | '__|  / _` | | | | | | | | '__|  / _` |");
        System.out.println("| |____  | | | |_  |  __/ | |    | (_| | | | | |_| | | |    | (_| |");
        System.out.println("|______| |_|  \\__|  \\___| |_|     \\__,_| |_|  \\__,_| |_|     \\__,_|");
        System.out.println();
    }

    private void mostrarMenu() {
        mostrarLogo();
        System.out.println("---- Menú Principal ----");
        System.out.println("1. Buscar un libro por título.");
        System.out.println("2. Listar los libros registrados.");
        System.out.println("3. Listar los autores registrados.");
        System.out.println("4. Listar los autores vivos en un año determinado.");
        System.out.println("5. Listar los libros por idioma.");
        System.out.println("0. Salir.");
    }

    private void procesaOpciones() {
        int opcion = -1;

        while (opcion != 0) {
            this.mostrarMenu();
            System.out.println();
            System.out.print("Seleccione una opción: ");

            if (scanner.hasNextInt()) {
                opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        listarLibrosRegistrados();
                        break;
                    case 3:
                        listarAutoresRegistrados();
                        break;
                    case 4:
                        listarAutoresVivosEnAnio();
                        break;
                    case 5:
                        listarLibrosPorIdioma();
                        break;
                    case 0:
                        System.out.println();
                        System.out.println("Saliendo del sistema.");
                        System.out.println();
                        break;
                    default:
                        System.out.println();
                        System.out.println("Opción no válida. Intente de nuevo.");
                        System.out.println();
                }
            } else {
                System.out.println();
                System.out.println("Error: Ingrese un número válido.");
                scanner.nextLine();
            }
            System.out.println();
        }
        scanner.close();
    }

    private void buscarLibroPorTitulo() {
        System.out.println();
        System.out.print("Ingrese el título del libro: ");
        String title = scanner.nextLine();

        try {
            Book book = bookService.searchBooksByTitle(title);

            if (book == null) {
                System.out.println();
                System.out.println("No se encontraron libros con ese título o el libro ya está registrado.");
                System.out.println();
            } else {
                mostrarDetallesLibro(book);
                bookService.saveBook(book);
                System.out.println();
                System.out.println("Libro guardado en la base de datos.");
                System.out.println();
            }
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listarLibrosRegistrados() {
        List<Book> books = bookService.getAllBooks();
        books.forEach(this::mostrarDetallesLibro);
    }

    private void listarAutoresRegistrados() {
        List<Author> authors = authorService.getAllAuthors();
        authors.forEach(this::mostrarDetallesAutor);
    }

    private void listarAutoresVivosEnAnio() {
        System.out.println();
        System.out.print("Ingrese el año (4 dígitos): ");
        String yearInput = scanner.nextLine();

        if (yearInput.length() != 4 || !yearInput.matches("\\d{4}")) {
            System.out.println();
            System.out.println("Error: Ingrese un año válido de 4 dígitos.");
            System.out.println();
            return;
        }

        int year = Integer.parseInt(yearInput);
        List<Author> authors = authorService.getAuthorsAliveInYear(year);
        authors.forEach(author -> System.out.println(author.getName()));
    }

    private void listarLibrosPorIdioma() {
        List<String> languages = bookService.getAvailableLanguages();

        if (languages.isEmpty()) {
            System.out.println();
            System.out.println("No hay libros registrados en la base de datos.");
            System.out.println();
            return;
        }

        System.out.println();
        System.out.println("Idiomas disponibles:");
        for (int i = 0; i < languages.size(); i++) {
            System.out.println((i + 1) + ". " + languages.get(i));
        }

        System.out.println();
        System.out.print("Seleccione un idioma (ingrese el número correspondiente): ");
        if (scanner.hasNextInt()) {
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice < 1 || choice > languages.size()) {
                System.out.println();
                System.out.println("Selección no válida.");
                System.out.println();
                return;
            }

            String selectedLanguage = languages.get(choice - 1);
            List<Book> books = bookService.getBooksByLanguage(selectedLanguage);

            if (books.isEmpty()) {
                System.out.println();
                System.out.println("No hay libros registrados en el idioma seleccionado.");
                System.out.println();
            } else {
                System.out.println();
                System.out.println("--- Libros en el idioma " + selectedLanguage + " ---");
                System.out.println();
                books.forEach(this::mostrarDetallesLibro);
            }
        } else {
            System.out.println();
            System.out.println("Error: Ingrese un número válido.");
            scanner.nextLine(); // Consume invalid input
        }
    }

    private void mostrarDetallesLibro(Book book) {
        System.out.println();
        System.out.println("--- Libro ---");
        System.out.println("Título: " + book.getTitle());
        System.out.println("Autor: " + book.getAuthor().getName());
        System.out.println("Idiomas: " + book.getLanguage());
        System.out.println("Número de Descargas: " + book.getDownloadCount());
        System.out.println();
    }

    private void mostrarDetallesAutor(Author author) {
        System.out.println();
        System.out.println("--- Autor ---");
        System.out.println("Nombre: " + author.getName());
        System.out.println("Año de nacimiento: " + author.getBirthYear());
        System.out.println("Año de fallecimiento: " + author.getDeathYear());
        System.out.println("Libros:");
        author.getBooks().forEach(this::mostrarDetallesLibro);
        System.out.println();
    }
}