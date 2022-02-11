package io.awspring.cloud.v3.dynamodb.core;

import io.awspring.cloud.v3.dynamodb.core.coverter.DynamoDbConverter;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

		return insert(objectToInsert, persistentEntity, persistentEntity.getTableName(), null, null, null);
	}

	PutItemRequest insert(Object objectToInsert,
						  DynamoDbPersistenceEntity<?> persistentEntity, String tableName, String conditionExpression,
						  Map<String, String> expressionAttributeNames, Map<String, Object> expressionAttributeValues) {

		Assert.notNull(tableName, "TableName must not be null");
		Assert.notNull(objectToInsert, "Object to insert must not be null");
		Assert.notNull(persistentEntity, "DynamoDbPersistenceEntity must not be null");


		Map<String, AttributeValue> object = new LinkedHashMap<>();
		dynamoDbConverter.write(objectToInsert, object, persistentEntity);
		PutItemRequest.Builder builder = PutItemRequest.builder().item(object).tableName(tableName);
		if (conditionExpression != null) {
			builder.conditionExpression(conditionExpression);
		}
		if (expressionAttributeNames != null) {
			builder.expressionAttributeNames(expressionAttributeNames);
		}
		if (expressionAttributeValues != null) {
			Map<String, AttributeValue> expressionAttributesToBuild = new HashMap<>(expressionAttributeValues.size());
			expressionAttributeValues.forEach((k, v) -> {
				expressionAttributesToBuild.put(k, dynamoDbConverter.convertToDynamoDbType(v, persistentEntity));
			});
		}


		return builder.build();
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

	DeleteItemRequest delete(Map<String, Object> keys, DynamoDbPersistenceEntity<?> requiredPersistentEntity, String tableName, String conditionExpression, Map<String, String> expressionAttributeNames, Map<String, Object> expressionAttributeValues) {
		Assert.notNull(tableName, "TableName must not be null");
		Assert.notNull(keys, "Keys to delete must not be null");
		Assert.notNull(requiredPersistentEntity, "DynamoDbPersistenceEntity must not be null");

		Map<String, AttributeValue> keysToBeUsed = new LinkedHashMap<>(keys.size());
		keys.forEach((k, v) -> {
			keysToBeUsed.put(k, dynamoDbConverter.convertToDynamoDbType(v, requiredPersistentEntity));
		});
		DeleteItemRequest.Builder deleteItemRequestBuilder = DeleteItemRequest.builder().tableName(tableName).key(keysToBeUsed);
		if (conditionExpression != null) {
			deleteItemRequestBuilder.conditionExpression(conditionExpression);
		}
		if (expressionAttributeNames != null) {
			deleteItemRequestBuilder.expressionAttributeNames(expressionAttributeNames);
		}
		if (expressionAttributeValues != null) {
			Map<String, AttributeValue> expressionAttributesToBuild = new HashMap<>(expressionAttributeValues.size());
			expressionAttributeValues.forEach((k, v) -> {
				expressionAttributesToBuild.put(k, dynamoDbConverter.convertToDynamoDbType(v, requiredPersistentEntity));
			});
		}


		return deleteItemRequestBuilder.build();
	}

	GetItemRequest findByKey(Object key, String tableName, DynamoDbPersistenceEntity<?> entity, Boolean consistentRead) {
		Assert.notNull(tableName, "TableName must not be null");
		Assert.notNull(key, "Key must not be null");
		Assert.notNull(entity, "DynamoDbPersistenceEntity must not be null");
		Map<String, AttributeValue> keys = new LinkedHashMap<>();
		dynamoDbConverter.findByKey(key, keys, entity);
		return GetItemRequest.builder().tableName(tableName).consistentRead(consistentRead)
			.key(keys).build();
	}


	ExecuteStatementRequest executeStatementRequest(String statement, String nextToken, List<Object> parameters,
													DynamoDbPersistenceEntity<?> entity, Boolean consistentRead) {
		Assert.notNull(statement, "Statement must not be null");
		Assert.notNull(entity, "DynamoDbPersistenceEntity must not be null");
		Map<String, AttributeValue> keys = new LinkedHashMap<>();

		ExecuteStatementRequest.Builder builder = ExecuteStatementRequest.builder().statement(statement);
		if (nextToken != null) {
			builder.nextToken(nextToken);
		}
		if (parameters != null) {
			List<AttributeValue> attributeValues = parameters.stream()
				.map(par -> dynamoDbConverter.convertToDynamoDbType(par, entity)).collect(Collectors.toList());
			builder.parameters(attributeValues);
		}
		if (consistentRead != null) {
			builder.consistentRead(consistentRead);
		}
		return builder.build();
	}

	GetItemRequest findByKeys(Map<String, Object> keysUsedForLookUp, String tableName, DynamoDbPersistenceEntity<?> entity, Boolean consistentRead) {
		Assert.notNull(tableName, "TableName must not be null");
		Assert.notNull(keysUsedForLookUp, "Keys must not be null");
		Assert.notNull(entity, "DynamoDbPersistenceEntity must not be null");
		Map<String, AttributeValue> keys = new LinkedHashMap<>();

		dynamoDbConverter.findByKeys(keysUsedForLookUp, keys, entity);
		return GetItemRequest.builder().tableName(tableName).consistentRead(consistentRead)
			.key(keys).build();
	}

	UpdateItemRequest update(Object objectToUpdate, String tableName, DynamoDbPersistenceEntity<?> entity,
							 String conditionExpression, Map<String, String> expressionAttributeNames,
							 Map<String, Object> expressionAttributeValues) {
		Assert.notNull(tableName, "TableName must not be null");
		Assert.notNull(objectToUpdate, "ObjectToUpdate must not be null");
		Assert.notNull(entity, "DynamoDbPersistenceEntity must not be null");


		Map<String, AttributeValue> keys = new LinkedHashMap<>();
		Map<String, AttributeValueUpdate> attributeUpdates = new LinkedHashMap<>();
		dynamoDbConverter.update(objectToUpdate, keys, entity, attributeUpdates);

		UpdateItemRequest.Builder builder = UpdateItemRequest.builder().tableName(tableName).key(keys);
		if (conditionExpression != null) {
			builder.conditionExpression(conditionExpression);
		}
		if (expressionAttributeNames != null) {
			builder.expressionAttributeNames(expressionAttributeNames);
		}
		if (expressionAttributeValues != null) {
			Map<String, AttributeValue> expressionAttributesToBuild = new HashMap<>(expressionAttributeValues.size());
			expressionAttributeValues.forEach((k, v) -> {
				expressionAttributesToBuild.put(k, dynamoDbConverter.convertToDynamoDbType(v, entity));
			});
		}
		return builder.build();
	}


	UpdateItemRequest update(Map<String, Object> keys, String updateExpression, String conditionExpression,
							 Map<String, String> expressionAttributeNames, String tableName,
							 DynamoDbPersistenceEntity<?> entity, Map<String, Object> expressionAttributeValues) {
		Assert.notNull(tableName, "TableName must not be null");
		Assert.notNull(keys, "Keys must not be null");
		Assert.notNull(entity, "DynamoDbPersistenceEntity must not be null");
		Assert.notNull(entity, "UpdateExpression must not be null");

		Map<String, AttributeValue> keysToBeUsed = new HashMap<>(keys.size());
		keys.forEach((k, v) -> {
			keysToBeUsed.put(k, dynamoDbConverter.convertToDynamoDbType(v, entity));
		});

		UpdateItemRequest.Builder builder = UpdateItemRequest.builder().tableName(tableName).key(keysToBeUsed).updateExpression(updateExpression).returnValues(ReturnValue.ALL_NEW);
		if (conditionExpression != null) {
			builder.conditionExpression(conditionExpression);
		}
		if (expressionAttributeNames != null) {
			builder.expressionAttributeNames(expressionAttributeNames);
		}
		if (expressionAttributeValues != null) {
			Map<String, AttributeValue> expressionAttributesToBuild = new HashMap<>(expressionAttributeValues.size());
			expressionAttributeValues.forEach((k, v) -> {
				expressionAttributesToBuild.put(k, dynamoDbConverter.convertToDynamoDbType(v, entity));
			});
		}
		return builder.build();
	}
}
