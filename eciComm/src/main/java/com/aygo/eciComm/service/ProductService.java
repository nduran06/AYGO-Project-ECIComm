package com.aygo.eciComm.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aygo.eciComm.exception.ProductValidationException;
import com.aygo.eciComm.model.Product;
import com.aygo.eciComm.model.enums.ProductStatus;
import com.aygo.eciComm.repository.ProductRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ProductService {

	private static final Logger LOG = LogManager.getLogger(ProductService.class);

	@Autowired
	private ProductRepository productRepository;
	private final S3Client s3Client;

	@Value("${aws.s3.bucket}")
	private String bucketName;

	public ProductService(ProductRepository productRepository, S3Client s3Client) {
		this.productRepository = productRepository;
		this.s3Client = s3Client;
	}

	public Product createProduct(Product product) {
		LOG.info("Creating new product: {}", product.getName());
		validateProduct(product);

		product.setProductStatus(ProductStatus.ACTIVE);
		product.setCreatedAt(Instant.now());

		try {
			return productRepository.save(product);
		} 
		
		catch (Exception e) {
			LOG.error("Error creating product: {}", e.getMessage(), e);
			throw new ProductValidationException("Failed to create product: " + e.getMessage());
		}
	}

	public Product getProduct(String id) {
		LOG.debug("Fetching product with id: {}", id);
		return productRepository.findById(id)
				.orElseThrow(() -> new ProductValidationException("Product: " + id + "not found"));
	}

	public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
		LOG.debug("Finding products in price range: {} to {}", minPrice, maxPrice);
		return productRepository.findByPriceRange(minPrice, maxPrice);
	}

	public Product updateProduct(String id, Product productUpdate) {
		LOG.info("Updating product with id: {}", id);

		Product existingProduct = getProduct(id);

		// Update only non-null fields
		if (productUpdate.getName() != null) {
			existingProduct.setName(productUpdate.getName());
		}
		
		if (productUpdate.getDescription() != null) {
			existingProduct.setDescription(productUpdate.getDescription());
		}
		
		if (productUpdate.getPrice() != null) {
			existingProduct.setPrice(productUpdate.getPrice());
		}
		
		if (productUpdate.getStockQuantity() != null) {
			existingProduct.setStockQuantity(productUpdate.getStockQuantity());
		}
		
		if (productUpdate.getCategory() != null) {
			existingProduct.setCategory(productUpdate.getCategory());
		}

		existingProduct.setUpdatedAt(Instant.now());

		validateProduct(existingProduct);
		return productRepository.save(existingProduct);
	}

	public void deleteProduct(String id) {
		LOG.info("Deleting product with id: {}", id);

		Product product = getProduct(id);

		// Delete image from S3 if exists
		if (product.getImageUrl() != null) {
			deleteProductImage(product.getImageUrl());
		}

		productRepository.delete(id);
	}

	public String uploadProductImage(String id, MultipartFile file) {
		LOG.info("Uploading image for product: {}", id);

		validateImageFile(file);
		Product product = getProduct(id);

		try {
			String key = String.format("products/%s/%s-%s", id, UUID.randomUUID().toString(),
					file.getOriginalFilename());

			// Delete old image if exists
			if (product.getImageUrl() != null) {
				deleteProductImage(product.getImageUrl());
			}

			// Upload new image
			PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(key)
					.contentType(file.getContentType()).build();

			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			// Update product with new image URL
			product.setImageUrl(key);
			productRepository.save(product);

			return key;			
		}
		
		catch (IOException e) {
			LOG.error("Error uploading product image: {}", e.getMessage(), e);
			throw new ProductValidationException("Failed to upload image: " + e.getMessage());
		}
	}

	private void validateProduct(Product product) {
		List<String> errors = new ArrayList<>();

		if (product.getName() == null || product.getName().trim().isEmpty()) {
			errors.add("Product name is required");
		}
		
		if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
			errors.add("Product price must be greater than zero");
		}
		
		if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
			errors.add("Stock quantity cannot be negative");
		}
		
		if (product.getCategory() == null) {
			errors.add("Product category is required");
		}

		if (!errors.isEmpty()) {
			throw new ProductValidationException(String.join(", ", errors));
		}
	}

	private void validateImageFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ProductValidationException("Image file is required");
		}
		if (file.getSize() > 5_000_000) { // 5MB limit
			throw new ProductValidationException("Image file size must be less than 5MB");
		}
		
		String contentType = file.getContentType();
		
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new ProductValidationException("File must be an image");
		}
	}

	private void deleteProductImage(String imageUrl) {
		try {
			DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(bucketName).key(imageUrl).build();
			s3Client.deleteObject(deleteRequest);
		} 
		
		catch (Exception e) {
			LOG.error("Error deleting product image: {}", e.getMessage(), e);
		}
	}
}