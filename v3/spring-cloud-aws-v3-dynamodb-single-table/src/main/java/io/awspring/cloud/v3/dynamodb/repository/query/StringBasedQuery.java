package io.awspring.cloud.v3.dynamodb.repository.query;

import io.awspring.cloud.v3.dynamodb.core.DynamoDbOperations;
import org.springframework.data.mapping.model.SpELExpressionEvaluator;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.spel.ExpressionDependencies;
import org.springframework.expression.ExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class StringBasedQuery {

	private final DynamoDbParameters parameters;

	private final ExpressionParser expressionParser;

	private final ExpressionQuery expressionQuery;

	private final ExpressionDependencies expressionDependencies;

	public StringBasedQuery(String query, DynamoDbParameters parameters, ExpressionParser expressionParser) {

		this.parameters = parameters;
		this.expressionParser = expressionParser;
		this.expressionQuery = ExpressionQuery.create(query);
		this.expressionDependencies = createExpressionDependencies();
	}

	private ExpressionDependencies createExpressionDependencies() {

		if (expressionQuery.getQueryParameterBindings().isEmpty()) {
			return ExpressionDependencies.none();
		}

		List<ExpressionDependencies> dependencies = new ArrayList<>();

		for (ExpressionQuery.ParameterBinding binding : expressionQuery.getQueryParameterBindings()) {
			dependencies.add(ExpressionDependencies.discover(expressionParser.parseExpression(binding.getExpression())));
		}

		return ExpressionDependencies.merged(dependencies);
	}

	public ExpressionDependencies getExpressionDependencies() {
		return expressionDependencies;
	}

	public String bindQuery(DynamoDbParameter parameterAccessor, SpELExpressionEvaluator evaluator) {
		return getSpelEvaluator(accessor).map(evaluator -> new ExpandedQuery(accessor, evaluator));
	}


	static class ParameterBinding {

		private final int parameterIndex;
		private final @Nullable String expression;
		private final @Nullable String parameterName;

		private ParameterBinding(int parameterIndex, @Nullable String expression, @Nullable String parameterName) {

			this.parameterIndex = parameterIndex;
			this.expression = expression;
			this.parameterName = parameterName;
		}

		static ParameterBinding expression(String expression, boolean quoted) {
			return new ParameterBinding(-1, expression, null);
		}

		static ParameterBinding indexed(int parameterIndex) {
			return new ParameterBinding(parameterIndex, null, null);
		}

		static ParameterBinding named(String name) {
			return new ParameterBinding(-1, null, name);
		}

		boolean isNamed() {
			return (parameterName != null);
		}

		int getParameterIndex() {
			return parameterIndex;
		}

		String getParameter() {
			return ("?" + (isExpression() ? "expr" : "") + parameterIndex);
		}

		String getRequiredExpression() {

			Assert.state(expression != null, "ParameterBinding is not an expression");
			return expression;
		}

		boolean isExpression() {
			return (this.expression != null);
		}

		String getRequiredParameterName() {

			Assert.state(parameterName != null, "ParameterBinding is not named");

			return parameterName;
		}
	}
}
