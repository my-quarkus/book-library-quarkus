package org.ciberaccion.booklibrary.service;

import org.ciberaccion.booklibrary.dto.AuthorDTO;
import org.ciberaccion.booklibrary.dto.PagedResponse;
import org.ciberaccion.booklibrary.entity.Author;
import org.ciberaccion.booklibrary.exception.BookLibraryException;
import org.ciberaccion.booklibrary.mapper.AuthorMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class AuthorService {

    @Inject
    AuthorMapper authorMapper;

    public PagedResponse<AuthorDTO.Response> findAll(int page, int size) {
        long total = Author.count();
        List<Author> authors = Author.findAll()
                .page(io.quarkus.panache.common.Page.of(page, size))
                .list();

        List<AuthorDTO.Response> dtos = authors.stream()
                .map(authorMapper::toResponse)
                .toList();

        return PagedResponse.of(dtos, page, size, total);
    }

    public AuthorDTO.Response findById(Long id) {
        Author author = Author.findById(id);
        if (author == null) {
            throw BookLibraryException.NotFoundException.author(id);
        }
        return authorMapper.toResponse(author);
    }

    public List<AuthorDTO.Response> searchByName(String name) {
        return Author.findByNameContaining(name)
                .stream()
                .map(authorMapper::toResponse)
                .toList();
    }

    @Transactional
    public AuthorDTO.Response create(AuthorDTO.Request request) {
        if (Author.existsByName(request.name)) {
            throw BookLibraryException.ConflictException.duplicateAuthorName(request.name);
        }
        Author author = authorMapper.toEntity(request);
        author.persist();
        return authorMapper.toResponse(author);
    }

    @Transactional
    public AuthorDTO.Response update(Long id, AuthorDTO.Request request) {
        Author author = Author.findById(id);
        if (author == null) {
            throw BookLibraryException.NotFoundException.author(id);
        }
        authorMapper.updateEntity(author, request);
        return authorMapper.toResponse(author);
    }

    @Transactional
    public void delete(Long id) {
        Author author = Author.findById(id);
        if (author == null) {
            throw BookLibraryException.NotFoundException.author(id);
        }
        author.delete();
    }
}
