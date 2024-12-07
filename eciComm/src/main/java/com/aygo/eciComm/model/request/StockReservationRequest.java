package com.aygo.eciComm.model.response.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StockReservationRequest {
	
	@NotNull(message = "Quantity is required")
	@Positive(message = "Quantity must be positive")
	private Integer quantity;

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}
