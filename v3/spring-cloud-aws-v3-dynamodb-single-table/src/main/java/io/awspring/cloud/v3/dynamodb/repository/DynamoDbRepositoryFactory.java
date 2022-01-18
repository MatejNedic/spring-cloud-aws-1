package io.awspring.cloud.v3.dynamodb.repository;

import io.awspring.cloud.v3.dynamodb.core.DynamoDbOperations;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistentProperty;
import io.awspring.cloud.v3.dynamodb.repository.query.CachingExpressionParser;
import io.awspring.cloud.v3.dynamodb.repository.query.DynamoDbQueryMethod;
import io.awspring.cloud.v3.dynamodb.repository.query.StringBasedDynamoDbQuery;
import io.awspring.cloud.v3.dynamodb.repository.support.DynamoDbEntityInformation;
import io.awspring.cloud.v3.dynamodb.repository.support.MappingDynamoDbEntityInformation;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

public class DynamoDbRepositoryFactory extends RepositoryFactorySupport {

	private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

	private final MappingContext<? extends DynamoDbPersistenceEntity<?>, DynamoDbPersistentProperty> mappingContext;

	private final DynamoDbOperations operations;

	public DynamoDbRepositoryFactory(DynamoDbOperations operations) {
		this.operations = operations;
		this.mappingContext = operations.getConverter().getMappingContext();
	}

	@Override
	public <T, ID> DynamoDbEntityInformation<T, ID> getEntityInformation(Class<T> clazz) {
		DynamoDbPersistenceEntity<?> dynamoDbPersistenceEntity = mappingContext.getRequiredPersistentEntity(clazz);
		return new MappingDynamoDbEntityInformation(dynamoDbPersistenceEntity, operations.getConverter());
	}

	@Override
	protected Object getTargetRepository(RepositoryInformation information) {
		DynamoDbEntityInformation<?, Object> entityInformation = getEntityInformation(information.getDomainType());

		return getTargetRepositoryViaReflection(information, entityInformation, operations);
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata repositoryMetadata) {
		return SimpleDynamoDbRepository.class;
	}

	public static class DynamoDbQueryLookUpStrategy implements QueryLookupStrategy {
		private final QueryMethodEvaluationContextProvider evaluationContextProvider;

		private final MappingContext<? extends DynamoDbPersistenceEntity<?>, DynamoDbPersistentProperty> mappingContext;

		private final DynamoDbOperations operations;

		private final ExpressionParser expressionParser = new CachingExpressionParser(EXPRESSION_PARSER);

		DynamoDbQueryLookUpStrategy(DynamoDbOperations operations,
									QueryMethodEvaluationContextProvider evaluationContextProvider,
									MappingContext<? extends DynamoDbPersistenceEntity<?>, DynamoDbPersistentProperty> mappingContext) {

			this.operations = operations;
			this.evaluationContextProvider = evaluationContextProvider;
			this.mappingContext = mappingContext;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
		 */
		@Override
		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
											NamedQueries namedQueries) {

			DynamoDbQueryMethod queryMethod = new DynamoDbQueryMethod(method, metadata, factory, mappingContext);
			String namedQueryName = queryMethod.getNamedQueryName();

			if (namedQueries.hasQuery(namedQueryName)) {
				String namedQuery = namedQueries.getQuery(namedQueryName);
				return new StringBasedDynamoDbQuery(namedQuery, queryMethod, operations, expressionParser,
					evaluationContextProvider);
			} else {
				return new StringBasedDynamoDbQuery(queryMethod, operations, expressionParser, evaluationContextProvider);
			}
		}
	}
}
