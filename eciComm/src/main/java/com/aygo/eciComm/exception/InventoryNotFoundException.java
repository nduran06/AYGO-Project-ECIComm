package com.aygo.eciComm.exception;

@SuppressWarnings("serial")
public class InventoryNotFoundException extends RuntimeException {

	public InventoryNotFoundException(String message) {
		super(message);
	}
}
