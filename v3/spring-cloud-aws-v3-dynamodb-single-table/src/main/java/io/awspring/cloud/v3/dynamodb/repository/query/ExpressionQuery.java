package io.awspring.cloud.v3.dynamodb.repository.query;

import org.springframework.data.repository.query.SpelQueryContext;

import java.util.ArrayList;
import java.util.List;

public class ExpressionQuery {

	private static final String SYNTHETIC_PARAMETER_TEMPLATE = "__dynamoDbSynthetic_%d__";
	private final String query;
	private final List<ParameterBinding> queryParameterBindings;

	private ExpressionQuery(String query, List<ParameterBinding> queryParameterBindings) {
		this.query = query;
		this.queryParameterBindings = queryParameterBindings;
	}

	public static ExpressionQuery create(String query) {

		List<ParameterBinding> parameterBindings = new ArrayList<>();

		SpelQueryContext queryContext = SpelQueryContext.of((counter, expression) -> {

			String parameterName = String.format(SYNTHETIC_PARAMETER_TEMPLATE, counter);
			parameterBindings.add(new ParameterBinding(parameterName, expression));
			return parameterName;
		}, String::concat);

		SpelQueryContext.SpelExtractor parsed = queryContext.parse(query);

		return new ExpressionQuery(parsed.getQueryString(), parameterBindings);
	}

	public String getQuery() {
		return query;
	}

	public List<ParameterBinding> getQueryParameterBindings() {
		return queryParameterBindings;
	}

	@Override
	public String toString() {
	return query;
	}

	static class ParameterBinding {

		private final String parameterName;
		private final String expression;

		private ParameterBinding(String parameterName, String expression) {

			this.expression = expression;
			this.parameterName = parameterName;
		}

		String getExpression() {
			return expression;
		}

		String getParameterName() {
			return parameterName;
		}
	}
}
