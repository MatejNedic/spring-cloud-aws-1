package io.awspring.cloud.v3.dynamodb.core.mapping.events;

public class DynamoDbAfterSaveEvent<T> extends DynamoDbMappingEvent{
	/**
	 * Create a new {@code ApplicationEvent}.
	 *
	 * @param source    the object on which the event initially occurred or with
	 *                  which the event is associated (never {@code null})
	 * @param tableName
	 */
	public DynamoDbAfterSaveEvent(Object source, String tableName) {
		super(source, tableName);
	}
}
