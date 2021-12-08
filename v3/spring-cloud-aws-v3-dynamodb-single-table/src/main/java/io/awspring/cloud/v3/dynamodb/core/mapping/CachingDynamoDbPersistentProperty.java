package io.awspring.cloud.v3.dynamodb.core.mapping;

import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

public class CachingDynamoDbPersistentProperty extends BasicDynamoDbPersistentProperty {

	private final boolean isPartitionKeyColumn;

	public CachingDynamoDbPersistentProperty(Property property, DynamoDbPersistenceEntity<?> owner,
											  SimpleTypeHolder simpleTypeHolder) {
		super(property, owner, simpleTypeHolder);
		isPartitionKeyColumn = super.isPartitionKeyColumn();
	}



	@Override
	public boolean isPartitionKeyColumn() {
		return isPartitionKeyColumn;
	}

}
