package io.awspring.cloud.v3.dynamodb.repository.support;

import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
import org.springframework.util.Assert;

public class SimpleDynamoDbEntityMetadata<T> implements DynamoDbEntityMetadata<T> {
	private final DynamoDbPersistenceEntity<?> entity;
	private final Class<T> type;

	public SimpleDynamoDbEntityMetadata(Class<T> type, DynamoDbPersistenceEntity<?> entity) {

		Assert.notNull(type, "Type must not be null");
		Assert.notNull(entity, "Collection entity must not be null or empty");

		this.type = type;
		this.entity = entity;
	}

	@Override
	public String getTableName() {
		return entity.getTableName();
	}

	@Override
	public Class<T> getJavaType() {
		return type;
	}
}
