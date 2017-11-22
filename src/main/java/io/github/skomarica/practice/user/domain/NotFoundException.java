package io.github.skomarica.practice.user.domain;

/**
 * @author Sinisa Komarica
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
