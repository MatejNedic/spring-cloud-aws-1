package io.awspring.cloud.v3.dynamodb.core;

import io.awspring.cloud.v3.dynamodb.core.coverter.DynamoDbConverter;
import io.awspring.cloud.v3.dynamodb.request.DynamoDBPageRequest;
import io.awspring.cloud.v3.dynamodb.request.QueryRequest;
import org.springframework.data.domain.Pageable;

import java.security.KeyPair;
import java.util.List;
import java.util.Map;

public interface DynamoDbOperations {
	<T> EntityWriteResult<T> save(T entity);
	<T> EntityWriteResult<T> save(T entity, String conditionExpression);
	<T> EntityWriteResult<T> save(T entity, String conditionExpression, Map<String, String> expressionAttributeNames, Map<String, Object> expressionAttributeValues);

	<T> Iterable<T> saveAll(Iterable<T> entities, Class entityClass);

	void delete(Object entity);
	<T> void delete(Class<T> entityClass, Map<String, Object> keys);
	<T> void delete(Class<T> entityClass, Map<String, Object> keys, String conditionalExpression);
	<T> void delete(Class<T> entityClass,Map<String, Object> keys, String conditionExpression, Map<String, String> expressionAttributeNames, Map<String, Object> expressionAttributeValues);

	DynamoDbConverter getConverter();

	<T> EntityReadResult<List<T>> query(Class<T> entityClass, QueryRequest queryRequest, DynamoDBPageRequest dynamoDBPageRequest);

	<T> EntityReadResult<List<T>> executeStatement(String statement, String nextToken, Class<T> entityClass, List<Object> values);
	<T> EntityReadResult<List<T>> executeStatement(String statement, String nextToken, Class<T> entityClass);
	<T> EntityReadResult<List<T>> executeStatement(String statement, String nextToken, Class<T> entityClass, List<Object> values, Boolean consistentRead);

	<T> T getEntityByKey(Object id, Class<T> entityClass);
	<T> T getEntityByKey(Object id, Class<T> entityClass, Boolean consistentRead);
	<T> T findEntityByKeys(Map<String, Object> mapOfKeys, Class<T> entityClass);
	<T> T findEntityByKeys(Map<String, Object> mapOfKeys, Class<T> entityClass, Boolean consistentRead);

	<T> EntityWriteResult<T> update(T entity);
	<T> EntityWriteResult<T> update(T entity, String conditionExpression);
	<T> EntityWriteResult<T> update(T entity, String conditionExpression, Map<String, String> expressionAttributeNames);
	<T> EntityWriteResult<T> update(T entity, String conditionExpression, Map<String, String> expressionAttributeNames, Map<String, Object> expressionAttributeValues);
	<T> EntityWriteResult<T> update(Map<String, Object> keys, String updateExpression, String conditionExpression, Map<String, String> expressionAttributeNames, Map<String, Object> expressionAttributeValues, Class<T> entityClass);
}
