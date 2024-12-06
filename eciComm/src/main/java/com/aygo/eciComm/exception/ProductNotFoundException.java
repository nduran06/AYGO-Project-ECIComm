package com.aygo.eciComm.exception;

@SuppressWarnings("serial")
public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException(String id) {
		super(String.format("Product not found with id: %s", id));
	}
}
