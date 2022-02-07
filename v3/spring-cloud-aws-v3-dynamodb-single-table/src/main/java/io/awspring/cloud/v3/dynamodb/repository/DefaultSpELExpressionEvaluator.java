package io.awspring.cloud.v3.dynamodb.repository;

import org.springframework.data.mapping.model.SpELExpressionEvaluator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;

public class DefaultSpELExpressionEvaluator implements SpELExpressionEvaluator {
	private ExpressionParser expressionParser;
	private EvaluationContext evaluationContext;

	public DefaultSpELExpressionEvaluator(ExpressionParser parser, EvaluationContext context) {
		this.expressionParser = parser;
		this.evaluationContext = context;
	}

	public static SpELExpressionEvaluator unsupported() {
		return NoOpExpressionEvaluator.INSTANCE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T evaluate(String expression) {
		return (T) expressionParser.parseExpression(expression).getValue(evaluationContext, Object.class);
	}

	enum NoOpExpressionEvaluator implements SpELExpressionEvaluator {

		INSTANCE;

		@Override
		public <T> T evaluate(String expression) {
			throw new UnsupportedOperationException("Expression evaluation not supported");
		}
	}
}
