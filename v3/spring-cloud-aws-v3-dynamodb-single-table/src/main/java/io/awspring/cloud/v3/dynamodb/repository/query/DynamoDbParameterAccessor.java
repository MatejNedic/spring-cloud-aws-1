package io.awspring.cloud.v3.dynamodb.repository.query;

import org.springframework.data.repository.query.ParameterAccessor;

public interface DynamoDbParameterAccessor extends ParameterAccessor {

	Object[] getValues();

	QueryOptions getQueryOptions();

	Class<?> getParameterType(int index);
}
