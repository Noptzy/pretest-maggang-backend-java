package com.pretest.ecommerce.exception;

import com.pretest.ecommerce.dto.WebResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<WebResponse<String>> handleValidationErrors(MethodArgumentNotValidException exception) {
                String errorMessage = exception.getBindingResult().getAllErrors().stream()
                                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                .collect(Collectors.joining(", "));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(WebResponse.<String>builder()
                                                .success(false)
                                                .message(errorMessage)
                                                .build());
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<WebResponse<String>> handleConstraintViolationException(
                        ConstraintViolationException exception) {
                String errorMessage = exception.getConstraintViolations().stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(WebResponse.<String>builder()
                                                .success(false)
                                                .message(errorMessage)
                                                .build());
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<WebResponse<String>> handleAPIException(ResponseStatusException exception) {
                return ResponseEntity.status(exception.getStatusCode())
                                .body(WebResponse.<String>builder()
                                                .success(false)
                                                .message(exception.getReason())
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<WebResponse<String>> handleGenericException(Exception exception) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(WebResponse.<String>builder()
                                                .success(false)
                                                .message("Internal Server Error")
                                                .errors(exception.getMessage())
                                                .build());
        }

        @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
        public ResponseEntity<WebResponse<String>> handleNoResourceFoundException(
                        org.springframework.web.servlet.resource.NoResourceFoundException exception) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(WebResponse.<String>builder()
                                                .success(false)
                                                .message("Not Found")
                                                .errors(exception.getMessage())
                                                .build());
        }

        @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
        public ResponseEntity<WebResponse<String>> handleNoHandlerFoundException(
                        org.springframework.web.servlet.NoHandlerFoundException exception) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(WebResponse.<String>builder()
                                                .success(false)
                                                .message("Not Found")
                                                .errors(exception.getMessage())
                                                .build());
        }
}