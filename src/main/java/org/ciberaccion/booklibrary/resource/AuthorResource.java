package org.ciberaccion.booklibrary.resource;

import org.ciberaccion.booklibrary.dto.AuthorDTO;
import org.ciberaccion.booklibrary.dto.PagedResponse;
import org.ciberaccion.booklibrary.service.AuthorService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/api/v1/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorResource {

    @Inject
    AuthorService authorService;

    @GET
    public PagedResponse<AuthorDTO.Response> findAll(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return authorService.findAll(page, size);
    }

    @GET
    @Path("/{id}")
    public AuthorDTO.Response findById(@PathParam("id") Long id) {
        return authorService.findById(id);
    }

    @GET
    @Path("/search")
    public List<AuthorDTO.Response> searchByName(@QueryParam("name") String name) {
        return authorService.searchByName(name);
    }

    @POST
    public Response create(@Valid AuthorDTO.Request request) {
        AuthorDTO.Response created = authorService.create(request);
        return Response
                .created(URI.create("/api/v1/authors/" + created.id))
                .entity(created)
                .build();
    }

    @PUT
    @Path("/{id}")
    public AuthorDTO.Response update(@PathParam("id") Long id, @Valid AuthorDTO.Request request) {
        return authorService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        authorService.delete(id);
        return Response.noContent().build();
    }
}
