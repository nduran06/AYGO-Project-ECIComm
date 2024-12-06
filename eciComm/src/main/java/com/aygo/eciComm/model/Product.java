package com.aygo.eciComm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.aygo.eciComm.model.enums.ProductCategory;
import com.aygo.eciComm.model.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

//The main Product entity
@DynamoDbBean
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product extends BaseItem {
	
	private String name;
	private String description;
	private BigDecimal price;
	private Integer stockQuantity;
	private String imageUrl;
	private ProductStatus productStatus;
	private ProductCategory category;
	private Set<String> tags;
	private Map<String, String> attributes;
	private BigDecimal weightInKg;
	private LocalDate releaseDate;
	private boolean featured;

	public Product() {
		setType("PRODUCT"); // Setting the discriminator for the base class
	}

	
	@DynamoDbPartitionKey
	@DynamoDbAttribute("productId")
	@JsonProperty("id")
	@Override
	public String getId() {
		return id;
	}
	
	// Core product information with validation
	@DynamoDbAttribute("name")
	@JsonProperty("name")
	@NotBlank(message = "Product name is required")
	@Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@DynamoDbAttribute("description")
	@JsonProperty("description")
	@Size(max = 2000, message = "Description cannot exceed 2000 characters")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@DynamoDbAttribute("price")
	@JsonProperty("price")
	@NotNull(message = "Price is required")
	@Positive(message = "Price must be greater than zero")
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@DynamoDbAttribute("stockQuantity")
	@JsonProperty("stockQuantity")
	@PositiveOrZero(message = "Stock quantity cannot be negative")
	public Integer getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(Integer stockQuantity) {
		this.stockQuantity = stockQuantity;
		// Automatically update status based on stock
		if (stockQuantity != null && stockQuantity == 0) {
			this.productStatus = ProductStatus.OUT_OF_STOCK;
		}
	}

	@DynamoDbAttribute("imageUrl")
	@JsonProperty("imageUrl")
	@Pattern(regexp = "^(https?://|products/).+", message = "Invalid image URL format")
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	@DynamoDbAttribute("productStatus")
	@JsonProperty("productStatus")
	public ProductStatus getProductStatus() {
		return productStatus;
	}

	public void setProductStatus(ProductStatus productStatus) {
		this.productStatus = productStatus;
	}

	@DynamoDbAttribute("category")
	@JsonProperty("category")
	@NotNull(message = "Product category is required")
	public ProductCategory getCategory() {
		return category;
	}

	public void setCategory(ProductCategory category) {
		this.category = category;
	}

	@DynamoDbAttribute("tags")
	@JsonProperty("tags")
	public Set<String> getTags() {
		return tags != null ? tags : new HashSet<>();
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	@DynamoDbAttribute("attributes")
	@JsonProperty("attributes")
	public Map<String, String> getAttributes() {
		return attributes != null ? attributes : new HashMap<>();
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	@DynamoDbAttribute("weightInKg")
	@JsonProperty("weightInKg")
	@PositiveOrZero(message = "Weight cannot be negative")
	public BigDecimal getWeightInKg() {
		return weightInKg;
	}

	public void setWeightInKg(BigDecimal weightInKg) {
		this.weightInKg = weightInKg;
	}

	@DynamoDbAttribute("releaseDate")
	@JsonProperty("releaseDate")
	@JsonFormat(pattern = "yyyy-MM-dd")
	public LocalDate getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
	}

	@DynamoDbAttribute("featured")
	@JsonProperty("featured")
	public boolean isFeatured() {
		return featured;
	}

	public void setFeatured(boolean featured) {
		this.featured = featured;
	}

	// Business logic methods
	public boolean isAvailable() {
		return ProductStatus.ACTIVE.equals(productStatus) && (stockQuantity != null && stockQuantity > 0);
	}

	public boolean canReduceStock(int quantity) {
		return stockQuantity != null && stockQuantity >= quantity;
	}

	public void reduceStock(int quantity) {
		if (!canReduceStock(quantity)) {
			throw new IllegalArgumentException("Insufficient stock");
		}
		setStockQuantity(stockQuantity - quantity);
	}

	public void addStock(int quantity) {
		if (quantity < 0) {
			throw new IllegalArgumentException("Cannot add negative stock");
		}
		setStockQuantity(stockQuantity != null ? stockQuantity + quantity : quantity);
		if (ProductStatus.OUT_OF_STOCK.equals(productStatus) && stockQuantity > 0) {
			setProductStatus(ProductStatus.ACTIVE);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Product other) {
			return super.equals(obj) && Objects.equals(name, other.name) && Objects.equals(price, other.price);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name, price);
	}

	// Enhanced toString using Java 21 string templates
	@Override
	public String toString() {
		return new StringBuilder(getClass().getSimpleName()).append(" {").append(getId()).append("', ").append("name='")
				.append(name).append("', ").append("price=").append(price).append(", ").append("status=")
				.append(productStatus).append(", ").append("category=").append(category).append(", ")
				.append("stockQuantity=").append(stockQuantity).append("}").toString();
	}
	
}