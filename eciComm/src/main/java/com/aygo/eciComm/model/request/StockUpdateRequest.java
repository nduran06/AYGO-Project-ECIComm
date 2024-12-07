package com.aygo.eciComm.model.request;

import jakarta.validation.constraints.NotNull;

public class StockUpdateRequest {
	
	@NotNull(message = "Quantity change is required")
	private Integer quantityChange;

	public Integer getQuantityChange() {
		return quantityChange;
	}

	public void setQuantityChange(Integer quantityChange) {
		this.quantityChange = quantityChange;
	}
}
