package org.ciberaccion.booklibrary.resource;

import java.net.URI;
import java.util.List;

import org.ciberaccion.booklibrary.dto.BookDTO;
import org.ciberaccion.booklibrary.dto.PagedResponse;
import org.ciberaccion.booklibrary.service.BookService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.*;

@Path("/api/v1/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    @Inject
    BookService bookService;

    @GET
    public PagedResponse<BookDTO.Response> findAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return bookService.findAll(page, size);
    }

    @GET
    @Path("/{id}")
    public BookDTO.Response findById(@PathParam("id") Long id) {
        return bookService.findById(id);
    }

    @GET
    @Path("/author/{authorId}")
    public List<BookDTO.Response> findByAuthor(@PathParam("authorId") Long authorId) {
        return bookService.findByAuthor(authorId);
    }

    @POST
    public Response create(@Valid BookDTO.Request request) {
        BookDTO.Response created = bookService.create(request);
        return Response
                .created(URI.create("/api/v1/books/" + created.id))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public BookDTO.Response update(@PathParam("id") Long id, @Valid BookDTO.Request request) {
        return bookService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        bookService.delete(id);
        return Response.noContent().build();
    }    
}
