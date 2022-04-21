/*
 * Copyright 2013-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.awspring.cloud.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

/**
 * Interface for simple DynamoDB template operations.
 *
 * @author Matej Nedic
 * @since 3.0.0
 */
public interface DynamoDBOperations {

	/**
	 * Saves Entity to DynamoDB table.
	 * @param entity - Entity to be saved.
	 * @param <T> Type of Entity object.
	 */
	<T extends TableNameProvider> T save(T entity);

	/**
	 * Updated Entity to DynamoDB table.
	 * @param entity - Entity to be saved.
	 * @param <T> Type of Entity object.
	 */
	<T extends TableNameProvider> T update(T entity);

	/**
	 * Deletes a record for a given Key.
	 * @param key to determine record in DynamoDB table.
	 * @param clazz Class of entity being deleted so {@link software.amazon.awssdk.enhanced.dynamodb.TableSchema} can be generated.
	 * @param tableName Name of Table which is being deleted upon on.
	 */
	void delete(Key key, Class<?> clazz, String tableName);

	/**
	 * Deletes a record for a given Entity.
	 * @param entity Entity object for deletion.
	 */
	<T extends TableNameProvider> void delete(T entity);

	/**
	 * Loads entity for a given Key.
	 * @param key to determine record in DynamoDB table.
	 * @param clazz Class of entity being fetched so {@link software.amazon.awssdk.enhanced.dynamodb.TableSchema} can be generated.
	 * @param <T> Type of Entity object.
	 * @param tableName Name of Table which entity will be fetched from.
	 */
	<T> T load(Key key, Class<T> clazz, String tableName);

	/**
	 * Queries a data for a given request.
	 * @param queryEnhancedRequest Request that is used by {@link software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient} to execute query request.
	 * @param clazz of entity being fetched so {@link software.amazon.awssdk.enhanced.dynamodb.TableSchema} can be generated.
	 * @param <T> Type of Entity object.
	 * @param tableName Name of Table which query will be preformed on.
	 * @return Iterable object which can be used to iterate pages and items.
	 */
	<T> PageIterable<T> query(QueryEnhancedRequest queryEnhancedRequest, Class<T> clazz, String tableName);

	/**
	 * Scans whole DynamoDB table.
	 *
	 * @param clazz of entity being fetched so {@link software.amazon.awssdk.enhanced.dynamodb.TableSchema} can be generated.
	 * @param tableName Name of Table which query will be preformed on.
	 * @param <T> type of Entity object.
	 * @return Iterable object which can be used to iterate pages and items.
	 */
	<T> PageIterable<T> scanAll(Class<T> clazz, String tableName);

	/**
	 * Scans Table using given request.
	 *
	 * @param scanEnhancedRequest request used by {@link software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient} to execute scan request.
	 * @param clazz of entity being fetched so {@link software.amazon.awssdk.enhanced.dynamodb.TableSchema} can be generated.
	 * @param tableName Name of Table which query will be preformed on.
	 * @param <T> type of Entity object.
	 @return Iterable object which can be used to iterate pages and items.
	 */
	<T> PageIterable<T> scan(ScanEnhancedRequest scanEnhancedRequest, Class<T> clazz, String tableName );
}
