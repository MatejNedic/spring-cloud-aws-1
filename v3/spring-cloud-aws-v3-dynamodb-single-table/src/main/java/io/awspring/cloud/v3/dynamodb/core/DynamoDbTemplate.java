package io.awspring.cloud.v3.dynamodb.core;

import io.awspring.cloud.v3.dynamodb.core.coverter.DynamoDbConverter;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
import io.awspring.cloud.v3.dynamodb.core.mapping.events.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DynamoDbTemplate used for Entity and DynamoDb communication.
 *
 * @author Matej Nedic
 * @since 3.0.0
 */
public class DynamoDbTemplate implements DynamoDbOperations, ApplicationContextAware, ApplicationEventPublisherAware {

	private DynamoDbClient dynamoDbClient;
	private DynamoDbConverter converter;
	private final EntityOperations entityOperations;
	private StatementFactory statementFactory;

	private @Nullable
	EntityCallbacks entityCallbacks;
	private ApplicationEventPublisher eventPublisher;


	public DynamoDbTemplate(DynamoDbClient dynamoDbClient, DynamoDbConverter converter) {
		this.dynamoDbClient = dynamoDbClient;
		this.converter = converter;
		this.entityOperations = new EntityOperations(converter.getMappingContext());
		this.statementFactory = new StatementFactory(converter);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (entityCallbacks == null) {
			setEntityCallbacks(EntityCallbacks.create(applicationContext));
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.eventPublisher = applicationEventPublisher;
	}

	public void setEntityCallbacks(@Nullable EntityCallbacks entityCallbacks) {
		this.entityCallbacks = entityCallbacks;
	}

	protected <E extends DynamoDbMappingEvent<T>, T> void maybeEmitEvent(E event) {

		if (this.eventPublisher != null) {
			this.eventPublisher.publishEvent(event);
		}
	}

	@Override
	public <T> Iterable<T> saveAll(Iterable<T> entities, Class ent) {
		List<WriteRequest> putRequests = new ArrayList<>();
		String tableName = getTableName(ent);
		Map<String, List<WriteRequest>> mapRequest = new HashMap<>();
		entities.forEach(entity -> {
			maybeEmitEvent(new DynamoDbBeforeSaveEvent<T>(entity, tableName));
			putRequests.add(WriteRequest.builder().putRequest(doSaveAll(entity, tableName)).build());
		});
		mapRequest.put(tableName, putRequests);
		BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder().requestItems(mapRequest).build();
		dynamoDbClient.batchWriteItem(batchWriteItemRequest);
		maybeEmitEvent(new DynamoDbAfterSaveEvent<>(entities, tableName));
		return entities;
	}

	@Override
	public <T> T getEntityByKey(Object id, Class<T> entityClass) {
		return getEntityByKey(id, entityClass, Boolean.FALSE);
	}

	@Override
	public <T> T findEntityByKeys(Map<String, Object> mapOfKeys, Class<T> entityClass) {
		return findEntityByKeys(mapOfKeys, entityClass, Boolean.FALSE);
	}

	@Override
	public <T> T findEntityByKeys(Map<String, Object> mapOfKeys, Class<T> entityClass, Boolean consistentRead) {
		Assert.notNull(mapOfKeys, "Must not be null");
		Assert.notNull(entityClass, "Entity type must not be null");

		DynamoDbPersistenceEntity<?> entity = getRequiredPersistentEntity(entityClass);
		String tableName = getTableName(entityClass);
		GetItemRequest getItemRequest = statementFactory.findByKeys(mapOfKeys, tableName, entity, consistentRead);
		GetItemResponse getItemResponse = dynamoDbClient.getItem(getItemRequest);
		return converter.read(entityClass, getItemResponse.item());
	}


	@Override
	public <T> T getEntityByKey(Object id, Class<T> entityClass, Boolean consistentRead) {

		Assert.notNull(id, "Id must not be null");
		Assert.notNull(entityClass, "Entity type must not be null");

		DynamoDbPersistenceEntity<?> entity = getRequiredPersistentEntity(entityClass);
		String tableName = getTableName(entityClass);
		GetItemRequest getItemRequest = statementFactory.findByKey(id, tableName, entity, consistentRead);
		GetItemResponse getItemResponse = dynamoDbClient.getItem(getItemRequest);
		return converter.read(entityClass, getItemResponse.item());
	}


	private <T> PutRequest doSaveAll(T entity, String tableName) {
		EntityOperations.AdaptibleEntity<T> source = getEntityOperations().forEntity(entity, getConverter().getConversionService());
		T entityToSave = maybeCallBeforeSave(entity, tableName);
		PutRequest request = statementFactory.insertAll(entityToSave, source.getPersistentEntity());
		maybeEmitEvent(new DynamoDbAfterSaveEvent<>(entityToSave, tableName));
		return request;
	}


	@Override
	public <T> EntityWriteResult<T> save(T entity, String conditionExpression) {
		return save(entity, conditionExpression, null, null);
	}

	@Override
	public <T> EntityWriteResult<T> save(T entity, String conditionExpression, Map<String, String> expressionAttributeNames,
										 Map<String, Object> expressionAttributeValues) {
		String tableName = getTableName(entity.getClass());
		maybeEmitEvent(new DynamoDbBeforeSaveEvent<T>(entity, tableName));
		EntityOperations.AdaptibleEntity<T> source = getEntityOperations().forEntity(entity, getConverter().getConversionService());
		T entityToSave = maybeCallBeforeSave(entity, tableName);
		PutItemRequest request = statementFactory.insert(entityToSave, source.getPersistentEntity(), tableName, conditionExpression, expressionAttributeNames, expressionAttributeValues);
		PutItemResponse putItemResponse = dynamoDbClient.putItem(request);
		maybeEmitEvent(new DynamoDbAfterSaveEvent<>(entityToSave, tableName));
		return EntityWriteResult.of(putItemResponse.attributes(), entity);
	}

	@Override
	public <T> EntityWriteResult<T> save(T entity) {
		return save(entity, Boolean.FALSE.toString());
	}


	@Override
	public void delete(Object entity) {
		String tableName = getTableName(entity.getClass());
		maybeEmitEvent(new DynamoDbBeforeDeleteEvent<>(entity, tableName));
		DeleteItemRequest request = statementFactory.delete(entity, getRequiredPersistentEntity(entity.getClass()), tableName);
		dynamoDbClient.deleteItem(request);
		maybeEmitEvent(new DynamoDbAfterDeleteEvent<>(entity, tableName));
	}

	@Override
	public <T> void delete(Class<T> entityClass, Map<String, Object> keys) {
	 delete(entityClass, keys, null);
	}

	@Override
	public <T> void delete(Class<T> entityClass, Map<String, Object> keys, String conditionalExpression) {
		delete(entityClass, keys, conditionalExpression, null, null);
	}

	@Override
	public <T> void delete(Class<T> entityClass, Map<String, Object> keys, String conditionExpression, Map<String, String> expressionAttributeNames, Map<String, Object> expressionAttributeValues) {
		String tableName = getTableName(entityClass.getClass());
		maybeEmitEvent(new DynamoDbBeforeDeleteEvent<>(entityClass, tableName));
		DeleteItemRequest request = statementFactory.delete(keys, getRequiredPersistentEntity(entityClass.getClass()), tableName, conditionExpression, expressionAttributeNames, expressionAttributeValues);
		dynamoDbClient.deleteItem(request);
		maybeEmitEvent(new DynamoDbAfterDeleteEvent<>(entityClass, tableName));
	}

	@Override
	public DynamoDbConverter getConverter() {
		return this.converter;
	}

	@Override
	public <T> EntityReadResult<List<T>> executeStatement(String statement, String nextToken, Class<T> entityClass, List<Object> values) {
		return executeStatement(statement,nextToken,entityClass,values,Boolean.FALSE);
	}

	@Override
	public <T> EntityReadResult<List<T>> executeStatement(String statement, String nextToken, Class<T> entityClass) {
		return executeStatement(statement,nextToken,entityClass,null);
	}

	@Override
	public <T> EntityReadResult<List<T>> executeStatement(String statement, String nextToken, Class<T> entityClass, List<Object> values, Boolean consistentRead) {
		Assert.notNull(statement, "Statement must not be null");
		Assert.notNull(entityClass, "Entity type must not be null");

		DynamoDbPersistenceEntity<?> entity = getRequiredPersistentEntity(entityClass);
		ExecuteStatementRequest executeStatementRequest = statementFactory.executeStatementRequest(statement,nextToken,values,entity,consistentRead);
		ExecuteStatementResponse executeStatementResponse = dynamoDbClient.executeStatement(executeStatementRequest);
		List<T> listToBeReturned = new ArrayList<>(executeStatementResponse.items().size());
		executeStatementResponse.items().forEach(item ->
			listToBeReturned.add(converter.read(entityClass, item))
			);
		return EntityReadResult.of(listToBeReturned, executeStatementResponse.nextToken());
	}

	public <T> EntityWriteResult<T> update(T entity) {
		return update(entity, null);
	}

	@Override
	public <T> EntityWriteResult<T> update(T entity, String conditionExpression) {
		return update(entity, conditionExpression, null);
	}

	@Override
	public <T> EntityWriteResult<T> update(T entity, String conditionExpression, Map<String, String> expressionAttributeNames) {
		return update(entity, conditionExpression, expressionAttributeNames, null);
	}

	@Override
	public <T> EntityWriteResult<T> update(T entity, String conditionExpression, Map<String,
		String> expressionAttributeNames, Map<String, Object> expressionAttributeValues) {
		String tableName = getTableName(entity.getClass());
		DynamoDbPersistenceEntity dynamoDbPersistenceEntity = getRequiredPersistentEntity(entity.getClass());
		UpdateItemRequest updateItemRequest = statementFactory.update(entity, tableName, dynamoDbPersistenceEntity,
			conditionExpression, expressionAttributeNames, expressionAttributeValues);
		maybeEmitEvent(new DynamoDbBeforeUpdateEvent<>(entity, tableName));
		UpdateItemResponse updateItemResponse = dynamoDbClient.updateItem(updateItemRequest);
		maybeEmitEvent(new DynamoDbAfterUpdateEvent<>(entity, tableName));
		return EntityWriteResult.of(updateItemResponse.attributes(), entity);
	}

	@Override
	public <T> EntityWriteResult<T> update(Map<String, Object> keys, String updateExpression, String conditionExpression,
										   Map<String, String> expressionAttributeNames, Map<String, Object> expressionAttributeValues,
										   Class<T> entityClass) {
		String tableName = getTableName(entityClass.getClass());
		DynamoDbPersistenceEntity dynamoDbPersistenceEntity = getRequiredPersistentEntity(entityClass.getClass());
		UpdateItemRequest updateItemRequest = statementFactory.update(keys, updateExpression,
			conditionExpression, expressionAttributeNames, tableName, dynamoDbPersistenceEntity, expressionAttributeValues);
		maybeEmitEvent(new DynamoDbBeforeUpdateEvent<>(entityClass, tableName));
		UpdateItemResponse updateItemResponse = dynamoDbClient.updateItem(updateItemRequest);
		maybeEmitEvent(new DynamoDbAfterUpdateEvent<>(entityClass, tableName));
		return EntityWriteResult.of(updateItemResponse.attributes(), converter.read(entityClass, updateItemResponse.attributes()));
	}


	public String getTableName(Class<?> entityClass) {
		return getEntityOperations().getTableName(entityClass);
	}

	protected EntityOperations getEntityOperations() {
		return this.entityOperations;
	}

	protected <T> T maybeCallBeforeSave(T object, String tableName) {

		if (null != entityCallbacks) {
			return (T) entityCallbacks.callback(DynamoDbBeforeSaveCallback.class, object, tableName);
		}

		return object;
	}

	private DynamoDbPersistenceEntity<?> getRequiredPersistentEntity(Class<?> entityType) {
		return getEntityOperations().getRequiredPersistentEntity(entityType);
	}

}

