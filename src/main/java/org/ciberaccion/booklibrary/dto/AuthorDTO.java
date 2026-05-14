package org.ciberaccion.booklibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthorDTO {

    public static class Request {

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100)
        public String name;

        @Size(max = 100)
        public String nationality;

        @Size(max = 500)
        public String biography;
    }

    public static class Response {
        public Long id;
        public String name;
        public String nationality;
        public String biography;
        public int bookCount;
    }

    public static class Summary {
        public Long id;
        public String name;
        public String nationality;
    }
}
