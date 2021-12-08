package io.awspring.cloud.v3.dynamodb.repository;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface DynamoDbRepository<T, KEY> {

	<S extends T> S save(S entity);

	/**
	 * Saves all given entities.
	 *
	 * @param entities must not be {@literal null} nor must it contain {@literal null}.
	 * @return the saved entities; will never be {@literal null}. The returned {@literal Iterable} will have the same size
	 *         as the {@literal Iterable} passed as an argument.
	 * @throws IllegalArgumentException in case the given {@link Iterable entities} or one of its entities is
	 *           {@literal null}.
	 */
	<S extends T> Iterable<S> saveAll(Iterable<S> entities);

	/**
	 * Retrieves an entity by its KEY.
	 *
	 * @param key must not be {@literal null}.
	 * @return the entity with the given KEY or {@literal Optional#empty()} if none found.
	 * @throws IllegalArgumentException if {@literal KEY} is {@literal null}.
	 */
	Optional<T> findByPartitionKey(KEY key);

	/**
	 * Returns whether an entity with the given KEY exists.
	 *
	 * @param key must not be {@literal null}.
	 * @return {@literal true} if an entity with the given KEY exists, {@literal false} otherwise.
	 * @throws IllegalArgumentException if {@literal KEY} is {@literal null}.
	 */
	boolean existsByKEY(KEY key);

	/**
	 * Returns all instances of the type.
	 *
	 * @return all entities
	 */
	Iterable<T> findAll();

	/**
	 * Returns all instances of the type {@code T} with the given KEYs.
	 * <p>
	 * If some or all KEYs are not found, no entities are returned for these KEYs.
	 * <p>
	 * Note that the order of elements in the result is not guaranteed.
	 *
	 * @param keys must not be {@literal null} nor contain any {@literal null} values.
	 * @return guaranteed to be not {@literal null}. The size can be equal or less than the number of given
	 *         {@literal KEYs}.
	 * @throws IllegalArgumentException in case the given {@link Iterable Keys (Objects Sor and Partition)} or one of its items is {@literal null}.
	 */
	Iterable<T> findAllByKeys(Iterable<Object> keys);

	/**
	 * Returns the number of entities available.
	 *
	 * @return the number of entities.
	 */
	long count();

	/**
	 * Deletes the entity with the given primaryKey.
	 *
	 * @param key must not be {@literal null}.
	 * @throws IllegalArgumentException in case the given {@literal KEY} is {@literal null}
	 */
	void deleteByKey(KEY key);

	/**
	 * Deletes a given entity.
	 *
	 * @param entity must not be {@literal null}.
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	void delete(T entity);
	

	/**
	 * Deletes the given entities.
	 *
	 * @param entities must not be {@literal null}. Must not contain {@literal null} elements.
	 * @throws IllegalArgumentException in case the given {@literal entities} or one of its entities is {@literal null}.
	 */
	void deleteAll(Iterable<? extends T> entities);

	/**
	 * Deletes all entities managed by the repository.
	 */
	void deleteAll();

}
