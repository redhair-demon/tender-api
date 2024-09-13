package io.codefresh.gradleexample.config;

import jakarta.servlet.ServletException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(value = {
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    protected ResponseEntity<ErrorResponse> handleConflict(RuntimeException e) {
        return error(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(value = {
            NoSuchElementException.class
    })
    protected ResponseEntity<ErrorResponse> handle404(NoSuchElementException e) {
        return error(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(value = {
            InvalidUserException.class
    })
    protected ResponseEntity<ErrorResponse> handle401(InvalidUserException e) {
        return error(HttpStatus.UNAUTHORIZED, e);
    }

    @ExceptionHandler(value = {
            InvalidRightsException.class
    })
    protected ResponseEntity<ErrorResponse> handle403(InvalidRightsException e) {
        return error(HttpStatus.FORBIDDEN, e);
    }

    @ExceptionHandler(value = {
            RuntimeException.class
    })
    protected ResponseEntity<ErrorResponse> handleRuntime(RuntimeException e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler(value = {
            ServletException.class
    })
    protected ResponseEntity<ErrorResponse> handleServlet(ServletException e) {
        return error(HttpStatus.BAD_REQUEST, e);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, Exception e) {
        log.error("Exception : ", e);
        return ResponseEntity.status(status).body(new ErrorResponse(e.getMessage()));
    }
    @AllArgsConstructor
    static class ErrorResponse {
        public String reason;
    }
}
