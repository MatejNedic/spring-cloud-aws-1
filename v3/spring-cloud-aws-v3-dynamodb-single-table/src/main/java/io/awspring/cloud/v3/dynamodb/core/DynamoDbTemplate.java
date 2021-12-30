package io.awspring.cloud.v3.dynamodb.core;

import io.awspring.cloud.v3.dynamodb.core.coverter.DynamoDbConverter;
import io.awspring.cloud.v3.dynamodb.core.mapping.events.*;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
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

import java.util.*;

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

	private @Nullable EntityCallbacks entityCallbacks;
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
		entities.forEach( entity -> {
			maybeEmitEvent(new DynamoDbBeforeSaveEvent<T>(entity, tableName));
			putRequests.add(WriteRequest.builder().putRequest(doSaveAll(entity, tableName)).build());
		});
		mapRequest.put(tableName, putRequests);
		BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder().requestItems(mapRequest).build();
		dynamoDbClient.batchWriteItem(batchWriteItemRequest);
		return null;
	}

	@Override
	public <T> T getEntityByKey(Object id, Class<T> entityClass) {

		Assert.notNull(id, "Id must not be null");
		Assert.notNull(entityClass, "Entity type must not be null");

		DynamoDbPersistenceEntity<?> entity = getRequiredPersistentEntity(entityClass);
		String tableName = getTableName(entityClass);
		GetItemRequest getItemRequest = statementFactory.findByKey(id, tableName, entity);
		GetItemResponse getItemResponse = dynamoDbClient.getItem(getItemRequest);
		return converter.read(entityClass, getItemResponse.item());
	}



	private <T> PutRequest doSaveAll(T entity, String tableName) {
		EntityOperations.AdaptibleEntity<T> source = getEntityOperations().forEntity(entity, getConverter().getConversionService());
		T entityToUse = source.isVersionedEntity() ? source.initializeVersionProperty() : entity;
		return doSaveAllUnVersioned(entityToUse, tableName, source);
	}

	private <T> PutRequest doSaveAllUnVersioned(T entityToUse, String tableName, EntityOperations.AdaptibleEntity<T> source) {
		T entityToSave = maybeCallBeforeSave(entityToUse, tableName);
		PutRequest request = statementFactory.insertAll(entityToSave, source.getPersistentEntity());
		maybeEmitEvent(new DynamoDbAfterSaveEvent<>(entityToSave, tableName));
		return request;
	}

	@Override
	public <T> EntityWriteResult<T> save(T entity) {
		String tableName = getTableName(entity.getClass());
		maybeEmitEvent(new DynamoDbBeforeSaveEvent<T>(entity, tableName));
		PutItemResponse putItemResponse = dynamoDbClient.putItem(doSave(entity, tableName));
		return EntityWriteResult.of(putItemResponse.attributes(), entity);
	}

	private <T> PutItemRequest doSave(T entity, String tableName) {
		EntityOperations.AdaptibleEntity<T> source = getEntityOperations().forEntity(entity, getConverter().getConversionService());
		T entityToUse = source.initializeVersionProperty();
		T entityToSave = maybeCallBeforeSave(entityToUse, tableName);
		PutItemRequest request = statementFactory.insert(entityToSave, source.getPersistentEntity(), tableName);
		maybeEmitEvent(new DynamoDbAfterSaveEvent<>(entityToSave, tableName));
		return request;
	}



	@Override
	public <KEY> void delete(Object entity, KEY key) {
		String tableName = getTableName(entity.getClass());
		maybeEmitEvent(new DynamoDbBeforeDelete<>(entity, tableName));
		DeleteItemRequest request = statementFactory.delete(entity, getRequiredPersistentEntity(entity.getClass()), tableName);
		dynamoDbClient.deleteItem(request);
	}

	@Override
	public DynamoDbConverter getConverter() {
		return this.converter;
	}

	public <T> EntityWriteResult<T> update(T entity) {
		String tableName = getTableName(entity.getClass());
		DynamoDbPersistenceEntity dynamoDbPersistenceEntity = getRequiredPersistentEntity(entity.getClass());
		UpdateItemRequest updateItemRequest = statementFactory.update(entity, tableName, dynamoDbPersistenceEntity);
		UpdateItemResponse updateItemResponse = dynamoDbClient.updateItem(updateItemRequest);
		return EntityWriteResult.of(updateItemResponse.attributes(), entity);
	}

	private <T> Object maybeCallBeforeConvert(T entity, String tableName) {
		return null;
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

