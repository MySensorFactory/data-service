package com.factory.controller;

import com.factory.exception.ClientErrorException;
import com.factory.exception.ServerErrorException;
import com.factory.openapi.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<Error> handleClientError(ClientErrorException ex) {
        var error = getError(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    private Error getError(final ClientErrorException ex) {
        return Error.builder()
                .code(Error.CodeEnum.fromValue(ex.getCode()))
                .description(ex.getMessage())
                .build();
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<Error> handleServerError(ServerErrorException ex) {
        var error = getError(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private Error getError(final ServerErrorException ex) {
        return Error.builder()
                .code(Error.CodeEnum.fromValue(ex.getCode()))
                .description(ex.getMessage())
                .build();
    }
}

