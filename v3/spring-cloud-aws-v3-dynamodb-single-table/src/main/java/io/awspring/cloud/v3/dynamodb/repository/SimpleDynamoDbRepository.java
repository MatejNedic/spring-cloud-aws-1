package io.awspring.cloud.v3.dynamodb.repository;

import io.awspring.cloud.v3.dynamodb.core.DynamoDbOperations;
import io.awspring.cloud.v3.dynamodb.core.mapping.BasicDynamoDbPersistenceEntity;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistentProperty;
import io.awspring.cloud.v3.dynamodb.repository.support.DynamoDbEntityInformation;
import org.springframework.data.mapping.context.MappingContext;

import java.util.Optional;

public class SimpleDynamoDbRepository<T, KEY> implements DynamoDbRepository<T, KEY> {


	private final MappingContext<? extends BasicDynamoDbPersistenceEntity<?>, DynamoDbPersistentProperty> mappingContext;
	private final DynamoDbOperations dynamoDbOperations;
	private final DynamoDbEntityInformation<T, KEY> entityInformation;

	public SimpleDynamoDbRepository(DynamoDbEntityInformation<T, KEY> entityInformation, DynamoDbOperations dynamoDbOperations, MappingContext<? extends BasicDynamoDbPersistenceEntity<?>, DynamoDbPersistentProperty> mappingContext) {
		this.entityInformation = entityInformation;
		this.mappingContext = mappingContext;
		this.dynamoDbOperations = dynamoDbOperations;
	}

	@Override
	public <S extends T> S save(S entity) {

		BasicDynamoDbPersistenceEntity<?> persistentEntity = this.mappingContext.getPersistentEntity(entity.getClass());

		if (persistentEntity != null && persistentEntity.hasVersionProperty()) {

			if (!entityInformation.isNew(entity)) {
				return this.dynamoDbOperations.update(entity).getEntity();
			}
		}

		return this.dynamoDbOperations.save(entity).getEntity();
	}

	@Override
	public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
		return (Iterable<S>) dynamoDbOperations.saveAll(entities, entityInformation.getJavaType());
	}

	@Override
	public Optional<T> findByPartitionKey(KEY key) {
		return Optional.empty();
	}

	@Override
	public boolean existsByKEY(KEY key) {
		return false;
	}

	@Override
	public Iterable<T> findAll() {
		return null;
	}

	@Override
	public Iterable<T> findAllByKeys(Iterable<Object> keys) {
		return null;
	}
	@Override
	public long count() {
		return 0;
	}

	@Override
	public void deleteByKey(KEY key) {
		dynamoDbOperations.delete(entityInformation.getJavaType(), key);
	}

	@Override
	public void delete(T entity) {

	}

	@Override
	public void deleteAll(Iterable<? extends T> entities) {

	}

	@Override
	public void deleteAll() {

	}
}
