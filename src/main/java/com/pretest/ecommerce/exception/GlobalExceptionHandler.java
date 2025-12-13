package com.pretest.ecommerce.exception;

import com.pretest.ecommerce.dto.WebResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
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
                                .message(errorMessage) // Masuk ke message
                                .build());
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<WebResponse<String>> handleConstraintViolationException(ConstraintViolationException exception) {
                String errorMessage = exception.getConstraintViolations().stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(WebResponse.<String>builder()
                                .success(false)
                                .message(errorMessage) // Masuk ke message
                                .build());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<WebResponse<String>> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
                String message = "Database error";
                if (exception.getRootCause() != null) {
                        String rootMessage = exception.getRootCause().getMessage();
                        if (rootMessage.toLowerCase().contains("duplicate")) {
                                message = "Data already exists (Email already registered)";
                        } else {
                                message = rootMessage;
                        }
                }

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(WebResponse.<String>builder()
                                .success(false)
                                .message(message)
                                .build());
        }

        @ExceptionHandler(TransactionSystemException.class)
        public ResponseEntity<WebResponse<String>> handleTransactionSystemException(TransactionSystemException exception) {
                Throwable rootCause = exception.getRootCause();
                String message = "Transaction Error";

                if (rootCause instanceof ConstraintViolationException) {
                        ConstraintViolationException constraintViolationException = (ConstraintViolationException) rootCause;
                        message = constraintViolationException.getConstraintViolations().stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(", "));
                } else if (rootCause != null) {
                        message = rootCause.getMessage();
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(WebResponse.<String>builder()
                                .success(false)
                                .message(message)
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
                                .message(exception.getMessage())
                                .build());
        }
}