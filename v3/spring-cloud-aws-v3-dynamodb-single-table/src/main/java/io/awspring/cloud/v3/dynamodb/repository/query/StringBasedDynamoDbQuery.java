package io.awspring.cloud.v3.dynamodb.repository.query;

import io.awspring.cloud.v3.dynamodb.core.DynamoDbOperations;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.expression.ExpressionParser;

public class StringBasedDynamoDbQuery {

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
}
