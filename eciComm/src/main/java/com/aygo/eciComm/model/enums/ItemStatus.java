package com.aygo.eciComm.model.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemStatus {

	ACTIVE("ACTIVE"), INACTIVE("INACTIVE"), DELETED("DELETED");

	private final String value;

	ItemStatus(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	@JsonCreator
	public static ItemStatus fromValue(String value) {
		return Arrays.stream(ItemStatus.values()).filter(status -> status.value.equals(value)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid status: " + value));
	}
}
