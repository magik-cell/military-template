package ua.edu.viti.military.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обробка ResourceNotFoundException (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse();
        error.setType("/errors/resource-not-found");
        error.setTitle("Resource Not Found");
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setDetail(ex.getMessage());
        error.setInstance(request.getDescription(false).replace("uri=", ""));
        error.setTimestamp(LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    /**
     * Обробка DuplicateResourceException (409 Conflict)
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            WebRequest request) {

        log.error("Duplicate resource: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse();
        error.setType("/errors/duplicate-resource");
        error.setTitle("Duplicate Resource");
        error.setStatus(HttpStatus.CONFLICT.value());
        error.setDetail(ex.getMessage());
        error.setInstance(request.getDescription(false).replace("uri=", ""));
        error.setTimestamp(LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    /**
     * Обробка BusinessLogicException (400 Bad Request)
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogic(
            BusinessLogicException ex,
            WebRequest request) {

        log.error("Business logic error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse();
        error.setType("/errors/business-logic");
        error.setTitle("Business Logic Violation");
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setDetail(ex.getMessage());
        error.setInstance(request.getDescription(false).replace("uri=", ""));
        error.setTimestamp(LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Обробка валідаційних помилок (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        });

        log.error("Validation errors: {}", validationErrors);

        ErrorResponse error = new ErrorResponse();
        error.setType("/errors/validation");
        error.setTitle("Validation Failed");
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setDetail("Помилки валідації вхідних даних");
        error.setInstance(request.getDescription(false).replace("uri=", ""));
        error.setTimestamp(LocalDateTime.now());
        error.setErrors(validationErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Обробка всіх інших помилок (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error occurred", ex);

        ErrorResponse error = new ErrorResponse();
        error.setType("/errors/internal");
        error.setTitle("Internal Server Error");
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setDetail("Внутрішня помилка сервера");
        error.setInstance(request.getDescription(false).replace("uri=", ""));
        error.setTimestamp(LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
