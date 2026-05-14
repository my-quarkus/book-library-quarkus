package org.ciberaccion.booklibrary.mapper;

import org.ciberaccion.booklibrary.dto.AuthorDTO;
import org.ciberaccion.booklibrary.entity.Author;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuthorMapper {

    public AuthorDTO.Response toResponse(Author author) {
        AuthorDTO.Response dto = new AuthorDTO.Response();
        dto.id = author.id;
        dto.name = author.name;
        dto.nationality = author.nationality;
        dto.biography = author.biography;
        dto.bookCount = author.books != null ? author.books.size() : 0;
        return dto;
    }

    public AuthorDTO.Summary toSummary(Author author) {
        AuthorDTO.Summary summary = new AuthorDTO.Summary();
        summary.id = author.id;
        summary.name = author.name;
        summary.nationality = author.nationality;
        return summary;
    }

    public Author toEntity(AuthorDTO.Request request) {
        Author author = new Author();
        author.name = request.name;
        author.nationality = request.nationality;
        author.biography = request.biography;
        return author;
    }

    public void updateEntity(Author author, AuthorDTO.Request request) {
        author.name = request.name;
        author.nationality = request.nationality;
        author.biography = request.biography;
    }
}
