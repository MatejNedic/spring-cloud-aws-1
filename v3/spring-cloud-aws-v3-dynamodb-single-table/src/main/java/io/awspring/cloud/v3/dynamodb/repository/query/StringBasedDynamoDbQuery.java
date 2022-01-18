package io.awspring.cloud.v3.dynamodb.repository.query;

import io.awspring.cloud.v3.dynamodb.core.DynamoDbOperations;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;

public class StringBasedDynamoDbQuery extends AbstractDynamoDbQuery {

	private final StringBasedQuery stringBasedQuery;
	private final ExpressionParser expressionParser;
	private final QueryMethodEvaluationContextProvider evaluationContextProvider;

	public StringBasedDynamoDbQuery(DynamoDbQueryMethod queryMethod, DynamoDbOperations operations,
									 ExpressionParser expressionParser, QueryMethodEvaluationContextProvider evaluationContextProvider) {

		this(queryMethod.getRequiredAnnotatedQuery(), queryMethod, operations, expressionParser, evaluationContextProvider);
	}


	public StringBasedDynamoDbQuery(String query, DynamoDbQueryMethod method, DynamoDbOperations operations,
									 ExpressionParser expressionParser, QueryMethodEvaluationContextProvider evaluationContextProvider) {
		super(method, operations);
		this.expressionParser = expressionParser;
		this.evaluationContextProvider = evaluationContextProvider;
		this.stringBasedQuery = new StringBasedQuery(query,
			method.getParameters(), expressionParser);
	}

	protected StringBasedQuery getStringBasedQuery() {
		return this.stringBasedQuery;
	}

	@Override
	public String createQuery(DynamoDbParameterAccessor parameterAccessor) {

		StringBasedQuery query = getStringBasedQuery();

		EvaluationContext evaluationContext = evaluationContextProvider.getEvaluationContext(
			getQueryMethod().getParameters(), parameterAccessor.getValues(), query.getExpressionDependencies());

		return getQueryStatementCreator().select(query, parameterAccessor,
			new DefaultSpELExpressionEvaluator(expressionParser, evaluationContext));
	}
}
