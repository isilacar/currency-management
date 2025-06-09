package com.openpayd.currency_management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class CurrencyGlobalExceptionHandler {

    @ExceptionHandler(CurrencySymbolNullException.class)
    public ResponseEntity<ErrorResponse> handleCurrencySymbolNull(CurrencySymbolNullException ex, WebRequest webRequest) {
        return new ResponseEntity<>(new ErrorResponse(
            getDate(),
            "NULL_CURRENCY_SYMBOL",
            ex.getMessage(),
            webRequest.getDescription(false)
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CurrencySymbolNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCurrencySymbolNotFound(CurrencySymbolNotFoundException ex, WebRequest webRequest) {
        return new ResponseEntity<>(new ErrorResponse(
            getDate(),
            "INVALID_CURRENCY_SYMBOL",
            ex.getMessage(),
            webRequest.getDescription(false)
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionParameterRequiredException.class)
    public ResponseEntity<ErrorResponse> handleTransactionParameterRequired(TransactionParameterRequiredException ex, WebRequest webRequest) {
        return new ResponseEntity<>(new ErrorResponse(
                getDate(),
                "TRANSACTION_PARAMETER_REQUIRED",
                ex.getMessage(),
                webRequest.getDescription(false)
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionHistoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionHistoryNotFoundException(TransactionHistoryNotFoundException ex, WebRequest webRequest) {
        return new ResponseEntity<>(new ErrorResponse(
                getDate(),
                String.valueOf(HttpStatus.NOT_FOUND.value()),
                ex.getMessage(),
                webRequest.getDescription(false)
        ), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleIDateTimeParseException(DateTimeParseException ex, WebRequest webRequest) {
        return new ResponseEntity<>(new ErrorResponse(
                getDate(),
                "INVALID_DATE_FORMAT",
                "Invalid date format. Please use yyyy-MM-dd format (e.g., 2025-06-24)",
                webRequest.getDescription(false)
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Validation error"
                ));

        ErrorResponse errorResponse = new ErrorResponse(
                getDate(),
                "VALIDATION_ERROR",
                "Validation failed for request parameters",
                String.join(", ", errors.values())
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private String getDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(dateTimeFormatter);
    }
}