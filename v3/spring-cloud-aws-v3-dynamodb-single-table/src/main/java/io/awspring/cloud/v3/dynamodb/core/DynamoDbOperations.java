package io.awspring.cloud.v3.dynamodb.core;

import io.awspring.cloud.v3.dynamodb.core.coverter.DynamoDbConverter;

import java.security.KeyPair;
import java.util.List;
import java.util.Map;

public interface DynamoDbOperations {
	<T> EntityWriteResult<T> save(T entity);
	<KEY> void delete(Object entity,KEY key);
	DynamoDbConverter getConverter();
	<T> Iterable<T> saveAll(Iterable<T> entities, Class ent);
	<T> T getEntityByKey(Object id, Class<T> entityClass);
	<T> EntityWriteResult<T> update(T entity);

}
