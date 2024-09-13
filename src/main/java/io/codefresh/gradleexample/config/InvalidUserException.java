package io.codefresh.gradleexample.config;

public class InvalidUserException extends RuntimeException {
    public InvalidUserException(String errorMessage) {
        super(errorMessage);
    }
}
