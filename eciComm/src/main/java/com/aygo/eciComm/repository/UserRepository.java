package com.aygo.eciComm.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.aygo.eciComm.model.User;
import com.aygo.eciComm.model.enums.UserStatus;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class UserRepository extends AbstractDynamoDBRepository<User> {

	public UserRepository(DynamoDbEnhancedClient enhancedClient,
			@Value("${aws.dynamodb.tables.user}") String tableName) {

		super(enhancedClient, User.class, tableName);
	}

	@Override
	protected Class<User> getEntityClass() {
		return User.class;
	}

	public Optional<User> findByEmail(String email) {
		Expression filterExpression = Expression.builder().expression("email = :email")
				.putExpressionValue(":email", AttributeValue.builder().s(email.toLowerCase()).build()).build();

		return getTable().scan(r -> r.filterExpression(filterExpression)).items().stream().findFirst();
	}

	public List<User> findByStatus(UserStatus status) {
		Expression filterExpression = Expression.builder().expression("#status = :status")
				.putExpressionName("#status", "status")
				.putExpressionValue(":status", AttributeValue.builder().s(status.name()).build()).build();

		return getTable().scan(r -> r.filterExpression(filterExpression)).items().stream().collect(Collectors.toList());
	}
}
