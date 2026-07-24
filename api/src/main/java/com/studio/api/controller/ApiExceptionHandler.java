package com.studio.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Turns bean-validation failures into a flat field -> message map the form
 * can render directly against its inputs.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> onValidationError(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fields.putIfAbsent(err.getField(), err.getDefaultMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("status", "invalid");
        body.put("message", "Check the highlighted fields and try again.");
        body.put("fields", fields);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
