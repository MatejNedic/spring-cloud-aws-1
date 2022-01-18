package io.awspring.cloud.v3.dynamodb.repository.query;

import io.awspring.cloud.v3.dynamodb.core.StatementFactory;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistentProperty;
import io.awspring.cloud.v3.dynamodb.core.query.Query;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.repository.query.parser.PartTree;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.function.Function;

public class QueryStatementCreator {
	private static final Log LOG = LogFactory.getLog(QueryStatementCreator.class);

	private final DynamoDbQueryMethod queryMethod;

	private final MappingContext<? extends DynamoDbPersistenceEntity<?>, DynamoDbPersistentProperty> mappingContext;

	QueryStatementCreator(DynamoDbQueryMethod queryMethod,
						  MappingContext<? extends DynamoDbPersistenceEntity<?>, DynamoDbPersistentProperty> mappingContext) {
		this.queryMethod = queryMethod;
		this.mappingContext = mappingContext;
	}

	private DynamoDbPersistenceEntity<?> getPersistentEntity() {
		return this.mappingContext.getRequiredPersistentEntity(this.queryMethod.getDomainClass());
	}

	String select(StatementFactory statementFactory, PartTree tree, DynamoDbParameterAccessor parameterAccessor,
				  ResultProcessor processor) {

		Function<Query, String> function = query -> {

			ReturnedType returnedType = processor.withDynamicProjection(parameterAccessor).getReturnedType();
			String statement = statementFactory.execute(query, getPersistentEntity());

			if (LOG.isDebugEnabled()) {
				LOG.debug(String.format("Created query [%s].", statement));
			}

			return statement;
		};

		return doWithQuery(parameterAccessor, tree, function);
	}

}
