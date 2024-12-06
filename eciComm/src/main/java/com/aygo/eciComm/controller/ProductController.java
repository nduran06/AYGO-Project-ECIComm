package com.aygo.eciComm.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aygo.eciComm.exception.ProductNotFoundException;
import com.aygo.eciComm.exception.ProductValidationException;
import com.aygo.eciComm.model.Product;
import com.aygo.eciComm.model.response.ErrorResponse;
import com.aygo.eciComm.service.ProductService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {

	@Autowired
	private ProductService productService;
	private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@PostMapping
	public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
		Product created = productService.createProduct(product);
		return ResponseEntity.created(URI.create("/api/v1/products/" + created.getId())).body(created);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Product> getProduct(@PathVariable String id) {
		Product product = productService.getProduct(id);
		return ResponseEntity.ok(product);
	}

	@GetMapping("/price-range")
	public ResponseEntity<List<Product>> findByPriceRange(@RequestParam @Positive BigDecimal minPrice,
			@RequestParam @Positive BigDecimal maxPrice) {
		List<Product> products = productService.findByPriceRange(minPrice, maxPrice);
		return ResponseEntity.ok(products);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Product> updateProduct(@PathVariable String id, @Valid @RequestBody Product product) {
		Product updated = productService.updateProduct(id, product);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
		productService.deleteProduct(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/image")
	public ResponseEntity<String> uploadImage(@PathVariable String id, @RequestParam("file") MultipartFile file) {
		String imageUrl = productService.uploadProductImage(id, file);
		return ResponseEntity.ok(imageUrl);
	}

	// Error handling
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
		ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(ProductValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidation(ProductValidationException ex) {
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), LocalDateTime.now());
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();

		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), String.join(", ", errors),
				LocalDateTime.now());
		return ResponseEntity.badRequest().body(error);
	}
}
