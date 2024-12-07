package com.aygo.eciComm.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aygo.eciComm.exception.InventoryNotFoundException;
import com.aygo.eciComm.exception.InventoryValidationException;
import com.aygo.eciComm.model.Inventory;
import com.aygo.eciComm.model.response.ErrorResponse;
import com.aygo.eciComm.model.response.request.StockReservationRequest;
import com.aygo.eciComm.model.response.request.StockUpdateRequest;
import com.aygo.eciComm.service.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
	
	private final InventoryService inventoryService;
	private static final Logger LOG = LoggerFactory.getLogger(InventoryController.class);

	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@PostMapping
	public ResponseEntity<Inventory> createInventory(@Valid @RequestBody Inventory inventory) {
		Inventory created = inventoryService.createInventory(inventory);
		return ResponseEntity.created(URI.create("/api/v1/inventory/" + created.getId())).body(created);
	}

	@GetMapping("/{inventoryId}")
	public ResponseEntity<Inventory> getInventory(@PathVariable String inventoryId) {
		return ResponseEntity.ok(inventoryService.getInventory(inventoryId));
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<Inventory> getInventoryByProduct(@PathVariable String productId) {
		return ResponseEntity.ok(inventoryService.getInventoryByProduct(productId));
	}

	@GetMapping("/low-stock")
	public ResponseEntity<List<Inventory>> getLowStockInventory() {
		return ResponseEntity.ok(inventoryService.getLowStockInventory());
	}

	@PatchMapping("/{inventoryId}/stock")
	public ResponseEntity<Inventory> updateStock(@PathVariable String inventoryId,
			@RequestBody @Valid StockUpdateRequest request) {
		return ResponseEntity.ok(inventoryService.updateStock(inventoryId, request.getQuantityChange()));
	}

	@PostMapping("/{inventoryId}/reserve")
	public ResponseEntity<Inventory> reserveStock(@PathVariable String inventoryId,
			@RequestBody @Valid StockReservationRequest request) {
		return ResponseEntity.ok(inventoryService.reserveStock(inventoryId, request.getQuantity()));
	}

	@PostMapping("/{inventoryId}/release")
	public ResponseEntity<Inventory> releaseReservedStock(@PathVariable String inventoryId,
			@RequestBody @Valid StockReservationRequest request) {
		return ResponseEntity.ok(inventoryService.releaseReservedStock(inventoryId, request.getQuantity()));
	}

	@ExceptionHandler(InventoryNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleInventoryNotFound(InventoryNotFoundException ex) {
		ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(InventoryValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidation(InventoryValidationException ex) {
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), LocalDateTime.now());
		return ResponseEntity.badRequest().body(error);
	}
}