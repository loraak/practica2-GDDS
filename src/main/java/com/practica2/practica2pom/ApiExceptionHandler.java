package com.practica2.practica2pom;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private ResponseEntity<ApiResponse> err(HttpStatus status, String msg) {
        return ResponseEntity.status(status)
                .body(new ApiResponse(status.value(), List.of(Map.of("error", String.valueOf(msg)))));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> invalid(MethodArgumentNotValidException e) {
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> Map.of("field", f.getField(), "error", String.valueOf(f.getDefaultMessage())))
                .toList();
        return ResponseEntity.badRequest().body(new ApiResponse(400, errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> badRequest(IllegalArgumentException e) {
        return err(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> badJson(HttpMessageNotReadableException e) {
        return err(HttpStatus.BAD_REQUEST, "JSON inválido");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse> notFound(NoSuchElementException e) {
        return err(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse> conflict(DataAccessException e) {
        return err(HttpStatus.CONFLICT, "Dato duplicado o referencia invalida");
    }
}