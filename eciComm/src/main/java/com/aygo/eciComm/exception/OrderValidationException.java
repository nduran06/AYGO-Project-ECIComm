package com.aygo.eciComm.exception;

@SuppressWarnings("serial")
public class OrderValidationException extends RuntimeException {
	
    public OrderValidationException(String message) {
        super(message);
    }
}
