package io.awspring.cloud.v3.dynamodb.core.mapping.events;

import org.springframework.context.ApplicationEvent;

public class DynamoDbMappingEvent<T> extends ApplicationEvent {
	private String tableName;
	/**
	 * Create a new {@code ApplicationEvent}.
	 *
	 * @param source the object on which the event initially occurred or with
	 *               which the event is associated (never {@code null})
	 * @param tableName
	 */
	public DynamoDbMappingEvent(Object source, String tableName) {
		super(source);
		this.tableName = tableName;
	}
}
