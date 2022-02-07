package io.awspring.cloud.v3.dynamodb.repository.query;

import org.springframework.data.mapping.model.SpELExpressionEvaluator;
import org.springframework.data.spel.ExpressionDependencies;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringBasedQuery {

	private final String query;
	private final DynamoDbParameters parameters;

	private final ExpressionParser expressionParser;

	private final ExpressionQuery expressionQuery;
	private final List<BindingContext.ParameterBinding> queryParameterBindings = new ArrayList<>();

	private final ExpressionDependencies expressionDependencies;

	public StringBasedQuery(String query, DynamoDbParameters parameters, ExpressionParser expressionParser) {
		this.query = query;
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

	public String bindQuery(DynamoDbParameterAccessor parameterAccessor, SpELExpressionEvaluator evaluator) {
		Assert.notNull(parameterAccessor, "DynamoDbParameterAccessor must not be null");
		Assert.notNull(evaluator, "SpELExpressionEvaluator must not be null");

		BindingContext bindingContext = new BindingContext(this.parameters, parameterAccessor, this.queryParameterBindings,
			evaluator);

		List<Object> arguments = bindingContext.getBindingValues();
		return ParameterBinder.INSTANCE.bind(this., arguments);
	}


	enum ParameterBinder {
		INSTANCE;

		private static final String ARGUMENT_PLACEHOLDER = "?_param_?";
		private static final Pattern ARGUMENT_PLACEHOLDER_PATTERN = Pattern.compile(Pattern.quote(ARGUMENT_PLACEHOLDER));

		public String bind(String input, List<Object> parameters) {

			if (parameters.isEmpty()) {
				return input;
			}

			StringBuilder result = new StringBuilder();

			int startIndex = 0;
			int currentPosition = 0;
			int parameterIndex = 0;

			Matcher matcher = ARGUMENT_PLACEHOLDER_PATTERN.matcher(input);

			while (currentPosition < input.length()) {

				if (!matcher.find()) {
					break;
				}

				int exprStart = matcher.start();

				result.append(input.subSequence(startIndex, exprStart)).append("?");

				parameterIndex++;
				currentPosition = matcher.end();
				startIndex = currentPosition;
			}

			return result.append(input.subSequence(currentPosition, input.length())).toString();
		}
	}
}
