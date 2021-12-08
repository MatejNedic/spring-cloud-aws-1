package io.awspring.cloud.v3.dynamodb.core.coverter;

import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
import org.springframework.data.convert.EntityWriter;
import org.springframework.lang.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
interface DynamoDbWriter<T> extends EntityWriter<T, Map<String, AttributeValue>> {
	Object convertToDynamoDbType(@Nullable Object obj, DynamoDbPersistenceEntity<?> entity);
}
