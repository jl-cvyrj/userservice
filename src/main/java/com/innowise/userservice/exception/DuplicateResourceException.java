package com.innowise.userservice.exception;

public class DuplicateResourceException extends ServiceException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}