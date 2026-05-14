package org.ciberaccion.booklibrary.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

public class GlobalExceptionMapper {

    public static class ErrorResponse {
        public int status;
        public String error;
        public String message;
        public LocalDateTime timestamp;
        public List<String> violations;

        public static ErrorResponse of(int status, String error, String message) {
            ErrorResponse e = new ErrorResponse();
            e.status = status;
            e.error = error;
            e.message = message;
            e.timestamp = LocalDateTime.now();
            return e;
        }
    }

    @Provider
    public static class NotFoundMapper
            implements ExceptionMapper<BookLibraryException.NotFoundException> {

        @Override
        public Response toResponse(BookLibraryException.NotFoundException ex) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ErrorResponse.of(404, "Not Found", ex.getMessage()))
                    .build();
        }
    }

    @Provider
    public static class ConflictMapper
            implements ExceptionMapper<BookLibraryException.ConflictException> {

        @Override
        public Response toResponse(BookLibraryException.ConflictException ex) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ErrorResponse.of(409, "Conflict", ex.getMessage()))
                    .build();
        }
    }

    @Provider
    public static class BadRequestMapper
            implements ExceptionMapper<BookLibraryException.BadRequestException> {

        @Override
        public Response toResponse(BookLibraryException.BadRequestException ex) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(ErrorResponse.of(400, "Bad Request", ex.getMessage()))
                    .build();
        }
    }

    @Provider
    public static class ValidationMapper
            implements ExceptionMapper<ConstraintViolationException> {

        @Override
        public Response toResponse(ConstraintViolationException ex) {
            List<String> messages = ex.getConstraintViolations()
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.toList());

            ErrorResponse error = ErrorResponse.of(
                    400,
                    "Validation Error",
                    "La petición contiene campos inválidos");
            error.violations = messages;

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error)
                    .build();
        }

    }

}
