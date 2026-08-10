package com.gekko.web;

import com.gekko.exception.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, WebRequest request) {

    List<FieldValidationError> fieldErrors = ex.getBindingResult().getFieldErrors()
        .stream()
        .map(fe -> new FieldValidationError(fe.getField(), fe.getDefaultMessage()))
        .collect(Collectors.toList());

    ApiError error = new ApiError();
    error.setStatus(HttpStatus.BAD_REQUEST.value());
    error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
    error.setMessage("Validation failed");
    error.setFieldErrors(fieldErrors);
    error.setPath(request.getDescription(false).replaceFirst("^uri=", ""));

    return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
    List<FieldValidationError> fieldErrors = ex.getConstraintViolations()
        .stream()
        .map(cv -> new FieldValidationError(cv.getPropertyPath().toString(), cv.getMessage()))
        .collect(Collectors.toList());

    ApiError error = new ApiError();
    error.setStatus(HttpStatus.BAD_REQUEST.value());
    error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
    error.setMessage("Validation failed");
    error.setFieldErrors(fieldErrors);
    error.setPath(request.getDescription(false).replaceFirst("^uri=", ""));

    return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  protected ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
    ApiError error = new ApiError();
    error.setStatus(HttpStatus.NOT_FOUND.value());
    error.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
    error.setMessage(ex.getMessage());
    error.setPath(request.getDescription(false).replaceFirst("^uri=", ""));
    return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
    ex.printStackTrace();
    ApiError error = new ApiError();
    error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    error.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    error.setMessage("An unexpected error occurred");
    error.setPath(request.getDescription(false).replaceFirst("^uri=", ""));
    return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
