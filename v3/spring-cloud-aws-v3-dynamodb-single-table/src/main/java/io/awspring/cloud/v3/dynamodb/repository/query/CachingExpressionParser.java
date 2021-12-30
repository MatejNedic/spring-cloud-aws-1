package io.awspring.cloud.v3.dynamodb.repository.query;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching expression parsers, so they are reused in expression evaluation once a query method is called.
 * Otherwise, new ExpressionParser would be created with every method invocation.
 * @author Matej Nedic
 * @since 3.0.0
 */
public class CachingExpressionParser implements ExpressionParser {

	private final ExpressionParser delegate;
	private final Map<String, Expression> cache = new ConcurrentHashMap<>();

	public CachingExpressionParser(ExpressionParser delegate) {
		this.delegate = delegate;
	}

	@Override
	public Expression parseExpression(String expression) throws ParseException {
		return cache.computeIfAbsent(expression, delegate::parseExpression);
	}

	@Override
	public Expression parseExpression(String expression, ParserContext parserContext) throws ParseException {
		throw new UnsupportedOperationException("Parsing using ParserContext is not supported");
	}
}
