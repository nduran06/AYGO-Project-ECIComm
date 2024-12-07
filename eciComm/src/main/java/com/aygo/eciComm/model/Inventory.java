package com.aygo.eciComm.model;

import java.util.Map;

import com.aygo.eciComm.model.enums.InventoryStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Inventory extends Component {

	private String productId;
	private Integer quantity;
	private Integer reservedQuantity;
	private Integer availableQuantity;
	private Integer reorderPoint;
	private Integer reorderQuantity;
	private String warehouseLocation;
	private InventoryStatus status;
	private String sku;
	private Map<String, String> metadata;

	@DynamoDbPartitionKey
	@DynamoDbAttribute("inventoryId")
	@JsonProperty("id")
	@Override
	public String getId() {
		return this.id;
	}

	@DynamoDbAttribute("productId")
	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	@DynamoDbAttribute("quantity")
	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
		updateAvailableQuantity();
		updateStatus();
	}

	@DynamoDbAttribute("reservedQuantity")
	public Integer getReservedQuantity() {
		return reservedQuantity;
	}

	public void setReservedQuantity(Integer reservedQuantity) {
		this.reservedQuantity = reservedQuantity;
		updateAvailableQuantity();
	}

	@DynamoDbAttribute("availableQuantity")
	public Integer getAvailableQuantity() {
		return availableQuantity;
	}

	public void setAvailableQuantity(Integer availableQuantity) {
		this.availableQuantity = availableQuantity;
	}

	@DynamoDbAttribute("reorderPoint")
	public Integer getReorderPoint() {
		return reorderPoint;
	}

	public void setReorderPoint(Integer reorderPoint) {
		this.reorderPoint = reorderPoint;
	}

	@DynamoDbAttribute("reorderQuantity")
	public Integer getReorderQuantity() {
		return reorderQuantity;
	}

	public void setReorderQuantity(Integer reorderQuantity) {
		this.reorderQuantity = reorderQuantity;
	}

	@DynamoDbAttribute("warehouseLocation")
	public String getWarehouseLocation() {
		return warehouseLocation;
	}

	public void setWarehouseLocation(String warehouseLocation) {
		this.warehouseLocation = warehouseLocation;
	}

	@DynamoDbAttribute("status")
	public InventoryStatus getInventoryStatus() {
		return status;
	}

	public void setStatus(InventoryStatus status) {
		this.status = status;
	}

	@DynamoDbAttribute("sku")
	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	@DynamoDbAttribute("metadata")
	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	/*
	 * @DynamoDbBeforeWrite public void beforeWrite() { super.beforeWrite(); if
	 * (this.inventoryId == null) { String timestamp =
	 * LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	 * String randomPart = UUID.randomUUID().toString().substring(0, 8);
	 * this.inventoryId = String.format("INV_%s_%s", timestamp, randomPart);
	 * this.setId(this.inventoryId); }
	 * 
	 * updateAvailableQuantity(); updateStatus(); }
	 */

	private void updateAvailableQuantity() {
		if (quantity != null) {
			this.availableQuantity = quantity - (reservedQuantity != null ? reservedQuantity : 0);
		}
	}

	private void updateStatus() {
		if (quantity == null || quantity == 0) {
			this.status = InventoryStatus.OUT_OF_STOCK;
		} else if (reorderPoint != null && quantity <= reorderPoint) {
			this.status = InventoryStatus.LOW_STOCK;
		} else {
			this.status = InventoryStatus.IN_STOCK;
		}
	}
}
