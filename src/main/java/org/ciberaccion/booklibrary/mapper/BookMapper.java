package org.ciberaccion.booklibrary.mapper;

import org.ciberaccion.booklibrary.dto.BookDTO;
import org.ciberaccion.booklibrary.entity.Author;
import org.ciberaccion.booklibrary.entity.Book;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BookMapper {

    @Inject
    AuthorMapper authorMapper;

    public BookDTO.Response toResponse(Book book) {
        BookDTO.Response dto = new BookDTO.Response();
        dto.id = book.id;
        dto.title = book.title;
        dto.isbn = book.isbn;
        dto.genre = book.genre;
        dto.pages = book.pages;
        dto.publishedDate = book.publishedDate;
        dto.description = book.description;
        dto.price = book.price;
        dto.author = authorMapper.toSummary(book.author);
        return dto;
    }

    public Book toEntity(BookDTO.Request request, Author author) {
        Book book = new Book();
        book.title = request.title;
        book.isbn = request.isbn;
        book.genre = request.genre;
        book.pages = request.pages;
        book.publishedDate = request.publishedDate;
        book.description = request.description;
        book.price = request.price;
        book.author = author;
        return book;
    }

    public void updateEntity(Book book, BookDTO.Request request, Author author) {
        book.title = request.title;
        book.isbn = request.isbn;
        book.genre = request.genre;
        book.pages = request.pages;
        book.publishedDate = request.publishedDate;
        book.description = request.description;
        book.price = request.price;
        book.author = author;
    }
}
