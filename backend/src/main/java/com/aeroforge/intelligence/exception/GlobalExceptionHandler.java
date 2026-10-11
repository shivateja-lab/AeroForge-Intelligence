package com.aeroforge.intelligence.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Map<String,Object>> authentication(AuthenticationException ex){ return error(HttpStatus.UNAUTHORIZED,"Invalid email or password"); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException ex){
        String message=ex.getBindingResult().getFieldErrors().stream().findFirst()
            .map(e->e.getField()+": "+e.getDefaultMessage()).orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST,message);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String,Object>> conflict(DataIntegrityViolationException ex){ return error(HttpStatus.CONFLICT,"A record with this code already exists"); }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String,Object>> status(ResponseStatusException ex){ return error(HttpStatus.valueOf(ex.getStatusCode().value()),ex.getReason()); }
    private ResponseEntity<Map<String,Object>> error(HttpStatus status,String message){
        Map<String,Object> body=new LinkedHashMap<>(); body.put("timestamp", Instant.now()); body.put("status",status.value()); body.put("error",status.getReasonPhrase()); body.put("message",message);
        return ResponseEntity.status(status).body(body);
    }
}
