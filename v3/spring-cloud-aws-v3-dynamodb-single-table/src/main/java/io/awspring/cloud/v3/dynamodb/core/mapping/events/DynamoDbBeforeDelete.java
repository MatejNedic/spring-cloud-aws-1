package io.awspring.cloud.v3.dynamodb.core.mapping.events;

public class DynamoDbBeforeDelete<T> extends DynamoDbMappingEvent {
public DynamoDbBeforeDelete(Object source, String tableName) {
	super(source, tableName);
}
}
