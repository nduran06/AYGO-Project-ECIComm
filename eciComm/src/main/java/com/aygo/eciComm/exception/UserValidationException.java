package com.aygo.eciComm.exception;

@SuppressWarnings("serial")
public class UserValidationException extends RuntimeException {
	
    public UserValidationException(String message) {
        super(message);
    }
}