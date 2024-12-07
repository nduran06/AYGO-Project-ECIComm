package com.aygo.eciComm.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.aygo.eciComm.model.Product;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class ProductRepository extends AbstractDynamoDBRepository<Product> {

	public ProductRepository(DynamoDbEnhancedClient enhancedClient,
			@Value("${aws.dynamodb.tables.product}") String tableName) {

		super(enhancedClient, Product.class, tableName);
	}

	@Override
	protected Class<Product> getEntityClass() {
		return Product.class;
	}

	// Add product-specific methods here
	public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
		var filterExpression = Expression.builder().expression("price BETWEEN :minPrice AND :maxPrice")
				.putExpressionValue(":minPrice", AttributeValue.builder().n(minPrice.toString()).build())
				.putExpressionValue(":maxPrice", AttributeValue.builder().n(maxPrice.toString()).build()).build();

		return getTable().scan(r -> r.filterExpression(filterExpression)).items().stream().toList();
	}
}
