package io.awspring.cloud.v3.dynamodb.core;

import io.awspring.cloud.v3.dynamodb.core.coverter.DynamoDbConverter;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatementFactory {


	private final DynamoDbConverter dynamoDbConverter;


	private final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();

	public StatementFactory(DynamoDbConverter dynamoDbConverter) {
		this.dynamoDbConverter = dynamoDbConverter;
	}


	public PutItemRequest insert(Object objectToInsert) {

		Assert.notNull(objectToInsert, "Object to builder must not be null");

		DynamoDbPersistenceEntity<?> persistentEntity = dynamoDbConverter.getMappingContext()
			.getRequiredPersistentEntity(objectToInsert.getClass());

		return insert(objectToInsert, persistentEntity, persistentEntity.getTableName());
	}

	PutItemRequest insert(Object objectToInsert,
						   DynamoDbPersistenceEntity<?> persistentEntity, String tableName) {

		Assert.notNull(tableName, "TableName must not be null");
		Assert.notNull(objectToInsert, "Object to insert must not be null");
		Assert.notNull(persistentEntity, "DynamoDbPersistenceEntity must not be null");


		Map<String, AttributeValue> object = new LinkedHashMap<>();
		dynamoDbConverter.write(objectToInsert, object, persistentEntity);

		return PutItemRequest.builder().item(object).tableName(tableName).build();
	}

	PutRequest insertAll(Object objectToInsert,
						  DynamoDbPersistenceEntity<?> persistentEntity) {

		Assert.notNull(objectToInsert, "Object to insert must not be null");
		Assert.notNull(persistentEntity, "DynamoDbPersistenceEntity must not be null");


		Map<String, AttributeValue> object = new LinkedHashMap<>();
		dynamoDbConverter.write(objectToInsert, object, persistentEntity);

		return PutRequest.builder().item(object).build();
	}

	DeleteItemRequest delete(Object objectToDelete, DynamoDbPersistenceEntity persistenceEntity, String tableName) {
		Assert.notNull(tableName, "TableName must not be null");
		Assert.notNull(objectToDelete, "Object to delete must not be null");
		Assert.notNull(persistenceEntity, "DynamoDbPersistenceEntity must not be null");

		Map<String, AttributeValue> keys = new LinkedHashMap<>();
		dynamoDbConverter.delete(objectToDelete, keys, persistenceEntity);

		return DeleteItemRequest.builder().tableName(tableName).key(keys).build();
	}

	GetItemRequest findByKey(Object key, String tableName, DynamoDbPersistenceEntity<?> entity) {
		Assert.notNull(tableName, "TableName must not be null");
		Assert.notNull(key, "Key must not be null");
		Assert.notNull(entity, "DynamoDbPersistenceEntity must not be null");
		Map<String, AttributeValue> keys = new LinkedHashMap<>();
		dynamoDbConverter.findByKey(key, keys, entity);
		return GetItemRequest.builder().tableName(tableName)
			.key(keys).build();
	}


}
