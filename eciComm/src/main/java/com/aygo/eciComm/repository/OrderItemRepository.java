package com.aygo.eciComm.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.aygo.eciComm.model.OrderItem;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class OrderItemRepository extends AbstractDynamoDBRepository<OrderItem> {

	public OrderItemRepository(DynamoDbEnhancedClient enhancedClient,
			@Value("${aws.dynamodb.tables.orderItem}") String tableName) {
		super(enhancedClient, OrderItem.class, tableName);
	}

	@Override
	protected Class<OrderItem> getEntityClass() {
		return OrderItem.class;
	}

	public List<OrderItem> findByOrderId(String orderId) {
		Expression filterExpression = Expression.builder().expression("orderId = :orderId")
				.putExpressionValue(":orderId", AttributeValue.builder().s(orderId).build()).build();

		return getTable().scan(r -> r.filterExpression(filterExpression)).items().stream().collect(Collectors.toList());
	}
}
