package com.aygo.eciComm.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.aygo.eciComm.model.Inventory;
import com.aygo.eciComm.model.enums.InventoryStatus;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class InventoryRepository extends AbstractDynamoDBRepository<Inventory> {

	public InventoryRepository(DynamoDbEnhancedClient enhancedClient,
			@Value("${aws.dynamodb.tables.inventory}") String tableName) {
		super(enhancedClient, Inventory.class, tableName);
	}

	@Override
	protected Class<Inventory> getEntityClass() {
		return Inventory.class;
	}

	public Optional<Inventory> findByProductId(String productId) {
		Expression filterExpression = Expression.builder().expression("productId = :productId")
				.putExpressionValue(":productId", AttributeValue.builder().s(productId).build()).build();

		// Convert SdkIterable to Stream before using findFirst
		return getTable().scan(r -> r.filterExpression(filterExpression)).items().stream().findFirst();
	}

	public List<Inventory> findByStatus(InventoryStatus status) {
		Expression filterExpression = Expression.builder().expression("#status = :status")
				.putExpressionName("#status", "status")
				.putExpressionValue(":status", AttributeValue.builder().s(status.name()).build()).build();

		// Convert SdkIterable to List
		return getTable().scan(r -> r.filterExpression(filterExpression)).items().stream().collect(Collectors.toList());
	}
}
