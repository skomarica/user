package io.github.skomarica.practice.user.web;

import io.github.skomarica.practice.user.domain.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Sinisa Komarica
 */
@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestError handleIllegalArgument(IllegalArgumentException ex) {
        return new RestError(ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public RestError handleNotFound(NotFoundException ex) {
        return new RestError(ex.getMessage());
    }

}
