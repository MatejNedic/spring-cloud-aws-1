package io.awspring.cloud.v3.dynamodb.repository.query;

import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.lang.Nullable;

public class DynamoDbParametersParameterAccessor extends ParametersParameterAccessor implements DynamoDbParameterAccessor {

	/**
	 * Creates a new {@link ParametersParameterAccessor}.
	 *
	 * @param method must not be {@literal null}.
	 * @param values     must not be {@literal null}.
	 */
	public DynamoDbParametersParameterAccessor(DynamoDbQueryMethod method, Object... values) {

		super(method.getParameters(), values);
	}

	@Override
	public Object[] getValues() {
		return super.getValues();
	}

	@Override
	public DynamoDbParameters getParameters() {
		return (DynamoDbParameters) super.getParameters();
	}

	@Override
	public Class<?> getParameterType(int index) {
		return getParameters().getParameter(index).getType();
	}

	@Nullable
	@Override
	public QueryOptions getQueryOptions() {

		int queryOptionsIndex = getParameters().getQueryOptionsIndex();

		Object value = (queryOptionsIndex != -1 ? getValue(queryOptionsIndex) : null);

		return (QueryOptions) value;
	}
}
