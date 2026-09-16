package com.shopsphere.order.exception;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InventoryServiceException.class)
    public ResponseEntity<ErrorResponse> handleInventory(InventoryServiceException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        String traceId = tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : null;
        return ResponseEntity.status(status).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now()).status(status.value())
                .error(status.getReasonPhrase()).message(message)
                .path(request.getRequestURI()).traceId(traceId).build());
    }
}
