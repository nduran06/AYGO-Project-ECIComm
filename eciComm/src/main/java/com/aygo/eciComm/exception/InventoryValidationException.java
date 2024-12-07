package com.aygo.eciComm.exception;

@SuppressWarnings("serial")
public class InventoryValidationException extends RuntimeException {

	public InventoryValidationException(String message) {
		super(message);
	}
}