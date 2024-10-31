package com.alura.literalura.repository;

import com.alura.literalura.model.Author;
import com.alura.literalura.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByLanguage(String language);
    boolean existsByTitleAndAuthor(String title, Author author);
    boolean existsByTitle(String title);
}