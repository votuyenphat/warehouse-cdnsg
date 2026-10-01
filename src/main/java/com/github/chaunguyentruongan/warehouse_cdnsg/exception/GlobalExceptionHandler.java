package com.github.chaunguyentruongan.warehouse_cdnsg.exception;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private String getPath(HttpServletRequest request) {
        return request.getRequestURI() != null ? request.getRequestURI() : request.getServletPath();
    }

    @ExceptionHandler(exception = {ResourceNotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ResponseExceptionDTO> handleNotFoundException(Exception ex,
            HttpServletRequest request) {
        ResponseExceptionDTO dto = new ResponseExceptionDTO();
        dto.setHttpStatus(404);
        dto.setError(ex.getMessage());
        dto.setTime(LocalDate.now());
        dto.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }

    @ExceptionHandler(exception = SqlDuplicateException.class)
    public ResponseEntity<ResponseExceptionDTO> handleSqlDuplicateException(SqlDuplicateException ex,
            HttpServletRequest request) {
        ResponseExceptionDTO dto = new ResponseExceptionDTO();
        dto.setHttpStatus(409);
        dto.setError(ex.getMessage());
        dto.setTime(LocalDate.now());
        dto.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(dto);
    }

    @ExceptionHandler(exception = ResourceExistsException.class)
    public ResponseEntity<ResponseExceptionDTO> handleResourceExistsException(
            ResourceExistsException ex,
            HttpServletRequest request) {
        ResponseExceptionDTO dto = new ResponseExceptionDTO();
        dto.setHttpStatus(409);
        dto.setError(ex.getMessage());
        dto.setTime(LocalDate.now());
        dto.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(dto);
    }

    @ExceptionHandler(exception = {TokenException.class, BadCredentialsException.class})
    public ResponseEntity<ResponseExceptionDTO> handleAuthException(Exception ex,
            HttpServletRequest request) {
        ResponseExceptionDTO dto = new ResponseExceptionDTO();
        dto.setHttpStatus(401);
        dto.setError(ex.getMessage());
        dto.setTime(LocalDate.now());
        dto.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto);
    }

    @ExceptionHandler(exception = IllegalArgumentException.class)
    public ResponseEntity<ResponseExceptionDTO> handleIllegalArgumentException(IllegalArgumentException ex,
            HttpServletRequest request) {
        ResponseExceptionDTO dto = new ResponseExceptionDTO();
        dto.setHttpStatus(400);
        dto.setError(ex.getMessage());
        dto.setTime(LocalDate.now());
        dto.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(exception = RuntimeException.class)
    public ResponseEntity<ResponseExceptionDTO> handleGenericRuntimeException(RuntimeException ex,
            HttpServletRequest request) {
        ResponseExceptionDTO dto = new ResponseExceptionDTO();
        dto.setHttpStatus(500);
        dto.setError(ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi hệ thống");
        dto.setTime(LocalDate.now());
        dto.setPath(getPath(request));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
    }
}
