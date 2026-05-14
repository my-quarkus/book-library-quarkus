package org.ciberaccion.booklibrary.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "books")
public class Book extends PanacheEntity {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 200)
    @Column(nullable = false, length = 200)
    public String title;

    @NotBlank(message = "El ISBN es obligatorio")
    @Column(nullable = false, unique = true, length = 20)
    public String isbn;

    @Size(max = 100)
    @Column(length = 100)
    public String genre;

    @Min(1)
    @Max(10000)
    public Integer pages;

    @Column(name = "published_date")
    public LocalDate publishedDate;

    @Size(max = 1000)
    @Column(length = 1000)
    public String description;

    @DecimalMin("0.0")
    public Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @NotNull(message = "El autor es obligatorio")
    public Author author;

    public static List<Book> findByAuthor(Long authorId) {
        return list("author.id", authorId);
    }

    public static boolean existsByIsbn(String isbn) {
        return count("isbn", isbn) > 0;
    }

    public static boolean existsByIsbnAndIdNot(String isbn, Long id) {
        return count("isbn = ?1 AND id != ?2", isbn, id) > 0;
    }
}