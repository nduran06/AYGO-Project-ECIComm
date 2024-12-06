package com.aygo.eciComm.model.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductStatus {

	DRAFT("DRAFT"), ACTIVE("ACTIVE"), OUT_OF_STOCK("OUT_OF_STOCK"), DISCONTINUED("DISCONTINUED");

	private final String value;

	ProductStatus(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	@JsonCreator
	public static ProductStatus fromValue(String value) {
		return Arrays.stream(ProductStatus.values()).filter(status -> status.value.equals(value)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid product status: " + value));
	}
}