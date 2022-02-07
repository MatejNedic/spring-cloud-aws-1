package io.awspring.cloud.v3.dynamodb.repository.query;

import io.awspring.cloud.v3.dynamodb.core.DynamoDbOperations;
import io.awspring.cloud.v3.dynamodb.core.coverter.DynamoDbConverter;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbMappingContext;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.util.Assert;

public abstract class AbstractDynamoDbQuery extends DynamoDbRepositoryQuerySupport {

	private final DynamoDbOperations operations;

	private static DynamoDbConverter toConverter(DynamoDbOperations operations) {

		Assert.notNull(operations, "DynamoDbOperations must not be null");

		return operations.getConverter();
	}

	private static DynamoDbMappingContext toMappingContext(DynamoDbOperations operations) {

		Assert.notNull(operations, "DynamoDbOperations must not be null");

		return toConverter(operations).getMappingContext();
	}

	public AbstractDynamoDbQuery(DynamoDbQueryMethod queryMethod, DynamoDbOperations operations) {

		super(queryMethod, toMappingContext(operations));

		this.operations = operations;
	}

	protected DynamoDbOperations getOperations() {
		return this.operations;
	}


	@Override
	public Object execute(Object[] parameters) {
		DynamoDbParameterAccessor parameterAccessor = new ConvertingParameterAccessor(toConverter(getOperations()),
			new DynamoDbParametersParameterAccessor(getQueryMethod(), parameters));

		ResultProcessor resultProcessor = getQueryMethod().getResultProcessor().withDynamicProjection(parameterAccessor);

		String statement = createQuery(parameterAccessor);

		DynamoDbQueryExecution queryExecution = getExecution(parameterAccessor);

		Class<?> resultType = resolveResultType(resultProcessor);

		return queryExecution.execute(statement, resultType);
	}

	private DynamoDbQueryExecution getExecution(DynamoDbParameterAccessor parameterAccessor) {

		return new DynamoDbQueryExecution.ResultProcessingExecution(getExecutionToWrap(parameterAccessor));
	}

	private DynamoDbQueryExecution getExecutionToWrap(DynamoDbParameterAccessor parameterAccessor) {
		return new DynamoDbQueryExecution.AttributeValueReturnQuery(getOperations());

	}

	protected abstract String createQuery(DynamoDbParameterAccessor accessor);

	private Class<?> resolveResultType(ResultProcessor resultProcessor) {

		DynamoDbReturnedType returnedType = new DynamoDbReturnedType(resultProcessor.getReturnedType(),
			getOperations().getConverter().getCustomConversions());

		return returnedType.getReturnedType();
	}
}
