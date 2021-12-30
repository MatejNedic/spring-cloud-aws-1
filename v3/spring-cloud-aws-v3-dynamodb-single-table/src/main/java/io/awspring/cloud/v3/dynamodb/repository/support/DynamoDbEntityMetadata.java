package io.awspring.cloud.v3.dynamodb.repository.support;

import org.springframework.data.repository.core.EntityMetadata;

public interface DynamoDbEntityMetadata<T> extends EntityMetadata<T> {


	String getTableName();
}
