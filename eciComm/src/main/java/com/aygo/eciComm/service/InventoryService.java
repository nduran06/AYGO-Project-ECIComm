package com.aygo.eciComm.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aygo.eciComm.exception.InventoryNotFoundException;
import com.aygo.eciComm.exception.InventoryValidationException;
import com.aygo.eciComm.model.Inventory;
import com.aygo.eciComm.model.enums.InventoryStatus;
import com.aygo.eciComm.repository.InventoryRepository;

@Service
public class InventoryService {

	private static final Logger LOG = LogManager.getLogger(InventoryService.class);

	private final InventoryRepository inventoryRepository;
	private static final Integer DEFAULT_REORDER_POINT = 10;
	private static final Integer DEFAULT_REORDER_QUANTITY = 50;

	public InventoryService(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}

	public Inventory createInventory(Inventory inventory) {
		LOG.info("Creating inventory for product: {}", inventory.getProductId());
		validateInventory(inventory);

		// Set defaults if not provided
		if (inventory.getReorderPoint() == null) {
			inventory.setReorderPoint(DEFAULT_REORDER_POINT);
		}
		if (inventory.getReorderQuantity() == null) {
			inventory.setReorderQuantity(DEFAULT_REORDER_QUANTITY);
		}
		if (inventory.getReservedQuantity() == null) {
			inventory.setReservedQuantity(0);
		}

		return inventoryRepository.save(inventory);
	}

	public Inventory getInventory(String inventoryId) {
		LOG.debug("Fetching inventory: {}", inventoryId);
		return inventoryRepository.findById(inventoryId)
				.orElseThrow(() -> new InventoryNotFoundException("Inventory not found: " + inventoryId));
	}

	public Inventory getInventoryByProduct(String productId) {
		LOG.debug("Fetching inventory for product: {}", productId);
		return inventoryRepository.findByProductId(productId)
				.orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product: " + productId));
	}

	public List<Inventory> getLowStockInventory() {
		return inventoryRepository.findByStatus(InventoryStatus.LOW_STOCK);
	}

	@Transactional
	public Inventory updateStock(String inventoryId, Integer quantityChange) {
		LOG.info("Updating stock for inventory {}: {}", inventoryId, quantityChange);
		Inventory inventory = getInventory(inventoryId);

		Integer newQuantity = inventory.getQuantity() + quantityChange;
		if (newQuantity < 0) {
			throw new InventoryValidationException("Insufficient stock available");
		}

		inventory.setQuantity(newQuantity);
		return inventoryRepository.save(inventory);
	}

	@Transactional
	public Inventory reserveStock(String inventoryId, Integer quantity) {
		LOG.info("Reserving {} items from inventory: {}", quantity, inventoryId);
		Inventory inventory = getInventory(inventoryId);

		if (inventory.getAvailableQuantity() < quantity) {
			throw new InventoryValidationException("Insufficient stock available for reservation");
		}

		inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
		return inventoryRepository.save(inventory);
	}

	@Transactional
	public Inventory releaseReservedStock(String inventoryId, Integer quantity) {
		LOG.info("Releasing {} reserved items from inventory: {}", quantity, inventoryId);
		Inventory inventory = getInventory(inventoryId);

		if (inventory.getReservedQuantity() < quantity) {
			throw new InventoryValidationException("Cannot release more items than are reserved");
		}

		inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
		return inventoryRepository.save(inventory);
	}

	private void validateInventory(Inventory inventory) {
		List<String> errors = new ArrayList<>();

		if (inventory.getProductId() == null || inventory.getProductId().trim().isEmpty()) {
			errors.add("Product ID is required");
		}
		if (inventory.getQuantity() == null || inventory.getQuantity() < 0) {
			errors.add("Quantity cannot be negative");
		}
		if (inventory.getWarehouseLocation() == null || inventory.getWarehouseLocation().trim().isEmpty()) {
			errors.add("Warehouse location is required");
		}

		if (!errors.isEmpty()) {
			throw new InventoryValidationException(String.join(", ", errors));
		}
	}
}
