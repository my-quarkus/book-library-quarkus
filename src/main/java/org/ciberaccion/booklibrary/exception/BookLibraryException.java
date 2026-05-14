package org.ciberaccion.booklibrary.exception;

public class BookLibraryException {

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }

        public static NotFoundException book(Long id) {
            return new NotFoundException("Libro con id " + id + " no encontrado");
        }

        public static NotFoundException author(Long id) {
            return new NotFoundException("Autor con id " + id + " no encontrado");
        }
    }

    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) {
            super(message);
        }

        public static ConflictException duplicateIsbn(String isbn) {
            return new ConflictException("Ya existe un libro con ISBN: " + isbn);
        }

        public static ConflictException duplicateAuthorName(String name) {
            return new ConflictException("Ya existe un autor con el nombre: " + name);
        }
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }
}
