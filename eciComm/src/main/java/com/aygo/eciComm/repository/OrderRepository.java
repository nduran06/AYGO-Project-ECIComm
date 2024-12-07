package com.aygo.eciComm.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.aygo.eciComm.model.Order;
import com.aygo.eciComm.model.Product;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class OrderRepository extends AbstractDynamoDBRepository<Order> {

	public OrderRepository(DynamoDbEnhancedClient enhancedClient,
			@Value("${aws.dynamodb.tables.order}") String tableName) {
		super(enhancedClient, Order.class, tableName);
	}

	@Override
	protected Class<Order> getEntityClass() {
		return Order.class;
	}

	public List<Order> findByUserId(String userId) {
		Expression filterExpression = Expression.builder().expression("userId = :userId")
				.putExpressionValue(":userId", AttributeValue.builder().s(userId).build()).build();

		return getTable().scan(r -> r.filterExpression(filterExpression)).items().stream().toList();
	}
}
