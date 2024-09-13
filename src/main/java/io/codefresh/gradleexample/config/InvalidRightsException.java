package io.codefresh.gradleexample.config;

public class InvalidRightsException extends RuntimeException {
    public InvalidRightsException(String errorMessage) {
        super(errorMessage);
    }
}
