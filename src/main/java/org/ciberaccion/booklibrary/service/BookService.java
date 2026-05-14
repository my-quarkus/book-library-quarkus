package org.ciberaccion.booklibrary.service;

import org.ciberaccion.booklibrary.dto.BookDTO;
import org.ciberaccion.booklibrary.dto.PagedResponse;
import org.ciberaccion.booklibrary.entity.Author;
import org.ciberaccion.booklibrary.entity.Book;
import org.ciberaccion.booklibrary.exception.BookLibraryException;
import org.ciberaccion.booklibrary.mapper.BookMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class BookService {

    @Inject
    BookMapper bookMapper;

    public PagedResponse<BookDTO.Response> findAll(int page, int size) {
        long total = Book.count();
        List<Book> books = Book.findAll()
                .page(io.quarkus.panache.common.Page.of(page, size))
                .list();

        List<BookDTO.Response> dtos = books.stream()
                .map(bookMapper::toResponse)
                .toList();

        return PagedResponse.of(dtos, page, size, total);
    }

    public BookDTO.Response findById(Long id) {
        Book book = Book.findById(id);
        if (book == null) {
            throw BookLibraryException.NotFoundException.book(id);
        }
        return bookMapper.toResponse(book);
    }

    public List<BookDTO.Response> findByAuthor(Long authorId) {
        if (Author.findById(authorId) == null) {
            throw BookLibraryException.NotFoundException.author(authorId);
        }
        return Book.findByAuthor(authorId).stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Transactional
    public BookDTO.Response create(BookDTO.Request request) {
        if (Book.existsByIsbn(request.isbn)) {
            throw BookLibraryException.ConflictException.duplicateIsbn(request.isbn);
        }
        Author author = Author.findById(request.authorId);
        if (author == null) {
            throw BookLibraryException.NotFoundException.author(request.authorId);
        }
        Book book = bookMapper.toEntity(request, author);
        book.persist();
        return bookMapper.toResponse(book);
    }

    @Transactional
    public BookDTO.Response update(Long id, BookDTO.Request request) {
        Book book = Book.findById(id);
        if (book == null) {
            throw BookLibraryException.NotFoundException.book(id);
        }
        if (Book.existsByIsbnAndIdNot(request.isbn, id)) {
            throw BookLibraryException.ConflictException.duplicateIsbn(request.isbn);
        }
        Author author = Author.findById(request.authorId);
        if (author == null) {
            throw BookLibraryException.NotFoundException.author(request.authorId);
        }
        bookMapper.updateEntity(book, request, author);
        return bookMapper.toResponse(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = Book.findById(id);
        if (book == null) {
            throw BookLibraryException.NotFoundException.book(id);
        }
        book.delete();
    }
}
