package com.aygo.eciComm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.aygo.eciComm.model.Component;
import com.aygo.eciComm.model.db.DynamoDBRepository;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public abstract class AbstractDynamoDBRepository<T extends Component> implements DynamoDBRepository<T> {

	private final DynamoDbEnhancedClient enhancedClient;
	private final DynamoDbTable<T> table;
	private final Class<T> entityClass;

	public AbstractDynamoDBRepository(DynamoDbEnhancedClient enhancedClient, Class<T> entityClass, String tableName) {

		this.enhancedClient = enhancedClient;
		this.entityClass = entityClass;
		this.table = this.enhancedClient.table(tableName, TableSchema.fromBean(this.entityClass));
	}

	protected abstract Class<T> getEntityClass();
	
	public DynamoDbTable<T> getTable(){
		return this.table;
	}

	@Override
	public T save(T item) {
		if (item.getId() == null) {
			item.setId(UUID.randomUUID().toString());
		}

		item.beforeWrite();
		table.putItem(item);
		return item;
	}

	@Override
	public Optional<T> findById(String id) {
		Key key = Key.builder().partitionValue(id).build();
		return Optional.ofNullable(table.getItem(key));
	}

	@Override
	public List<T> findAll() {
		return table.scan().items().stream().collect(Collectors.toList());
	}

	@Override
	public void delete(String id) {
		Key key = Key.builder().partitionValue(id).build();
		table.deleteItem(key);
	}

	@Override
	public List<T> findByType(String type) {
		Expression filterExpression = Expression.builder().expression("#type = :type")
				.putExpressionName("#type", "type")
				.putExpressionValue(":type", AttributeValue.builder().s(type).build()).build();

		ScanEnhancedRequest request = ScanEnhancedRequest.builder().filterExpression(filterExpression).build();

		return table.scan(request).items().stream().collect(Collectors.toList());
	}
}