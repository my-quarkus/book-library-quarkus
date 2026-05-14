package org.ciberaccion.booklibrary.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class BookDTO {

    public static class Request {

        @NotBlank(message = "El título es obligatorio")
        @Size(min = 1, max = 200)
        public String title;

        @NotBlank(message = "El ISBN es obligatorio")
        public String isbn;

        @Size(max = 100)
        public String genre;

        @Min(1)
        @Max(10000)
        public Integer pages;

        public LocalDate publishedDate;

        @Size(max = 1000)
        public String description;

        @DecimalMin("0.0")
        public Double price;

        @NotNull(message = "El authorId es obligatorio")
        public Long authorId;
    }

    public static class Response {
        public Long id;
        public String title;
        public String isbn;
        public String genre;
        public Integer pages;
        public LocalDate publishedDate;
        public String description;
        public Double price;
        public AuthorDTO.Summary author;
    }
}
