package io.awspring.cloud.v3.dynamodb.repository.query;

import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistentProperty;
import io.awspring.cloud.v3.dynamodb.repository.support.DynamoDbEntityMetadata;
import io.awspring.cloud.v3.dynamodb.repository.support.SimpleDynamoDbEntityMetadata;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Optional;

public class DynamoDbQueryMethod extends QueryMethod {

	private final Method method;

	private final MappingContext<? extends DynamoDbPersistenceEntity<?>, ? extends DynamoDbPersistentProperty> mappingContext;

	private final Optional<Query> query;


	private @Nullable
	DynamoDbEntityMetadata<?> entityMetadata;

	public DynamoDbQueryMethod(Method method, RepositoryMetadata repositoryMetadata, ProjectionFactory projectionFactory,
							   MappingContext<? extends DynamoDbPersistenceEntity<?>, ? extends DynamoDbPersistentProperty> mappingContext) {

		super(method, repositoryMetadata, projectionFactory);

		Assert.notNull(mappingContext, "MappingContext must not be null");

		verify(method, repositoryMetadata);

		this.method = method;
		this.mappingContext = mappingContext;
		this.query = Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(method, Query.class));
	}

	public void verify(Method method, RepositoryMetadata metadata) {

		if (isPageQuery()) {
			throw new InvalidDataAccessApiUsageException("Page queries are not supported. Use a Slice query.");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public DynamoDbEntityMetadata<?> getEntityInformation() {

		if (this.entityMetadata == null) {

			Class<?> returnedObjectType = getReturnedObjectType();
			Class<?> domainClass = getDomainClass();

			if (ClassUtils.isPrimitiveOrWrapper(returnedObjectType)) {
				this.entityMetadata = new SimpleDynamoDbEntityMetadata<>((Class<Object>) domainClass,
					this.mappingContext.getRequiredPersistentEntity(domainClass));

			} else {

				DynamoDbPersistenceEntity<?> returnedEntity = this.mappingContext.getPersistentEntity(returnedObjectType);
				DynamoDbPersistenceEntity<?> managedEntity = this.mappingContext.getRequiredPersistentEntity(domainClass);

				returnedEntity = returnedEntity == null || returnedEntity.getType().isInterface() ? managedEntity
					: returnedEntity;

				this.entityMetadata = new SimpleDynamoDbEntityMetadata<>((Class<Object>) returnedEntity.getType(),
					managedEntity);
			}
		}

		return this.entityMetadata;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.query.QueryMethod#getParameters()
	 */
	@Override
	public DynamoDbParameters getParameters() {
		return (DynamoDbParameters) super.getParameters();
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.repository.query.QueryMethod#createParameters(java.lang.reflect.Method)
	 */
	@Override
	protected DynamoDbParameters createParameters(Method method) {
		return new DynamoDbParameters(method);
	}

	/**
	 * Returns whether the method has an annotated query.
	 */
	public boolean hasAnnotatedQuery() {
		return this.query.map(Query::value).filter(StringUtils::hasText).isPresent();
	}

	/**
	 * Returns the query string declared in a {@link Query} annotation or {@literal null} if neither the annotation found
	 * nor the attribute was specified.
	 *
	 * @return the query string or {@literal null} if no query string present.
	 */
	@Nullable
	public String getAnnotatedQuery() {
		return this.query.map(Query::value).orElse(null);
	}


	/**
	 * Returns the required query string declared in a {@link Query} annotation or throws {@link IllegalStateException} if
	 * neither the annotation found nor the attribute was specified.
	 *
	 * @return the query string.
	 * @throws IllegalStateException in case query method has no annotated query.
	 */
	public String getRequiredAnnotatedQuery() {
		return this.query.map(Query::value)
			.orElseThrow(() -> new IllegalStateException("Query method " + this + " has no annotated query"));
	}

	/**
	 * Returns the {@link Query} annotation that is applied to the method or {@literal null} if none available.
	 *
	 * @return the optional query annotation.
	 */
	Optional<Query> getQueryAnnotation() {
		return this.query;
	}

	@Override
	protected Class<?> getDomainClass() {
		return super.getDomainClass();
	}

	/**
	 * @return the return type for this {@link QueryMethod}.
	 */
	public TypeInformation<?> getReturnType() {
		return ClassTypeInformation.fromReturnTypeOf(this.method);
	}


}
