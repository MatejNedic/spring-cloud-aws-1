package io.awspring.cloud.v3.dynamodb.core.mapping;

import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class CachingDynamoDbPersistentProperty extends BasicDynamoDbPersistentProperty {

	private final boolean isPartitionKeyColumn;
	private final boolean isEmbedded;
	private final Class typeOfProperty;

	public CachingDynamoDbPersistentProperty(Property property, DynamoDbPersistenceEntity<?> owner,
											 SimpleTypeHolder simpleTypeHolder) {
		super(property, owner, simpleTypeHolder);
		this.isEmbedded = super.isEntity();
		if (isEmbedded) {
			try {
				Type ty = property.getField().get().getGenericType();
				if (ty instanceof ParameterizedType) {
					Type t = ((ParameterizedType) ty).getActualTypeArguments()[0];
					this.typeOfProperty =  Class.forName(((Class) t).getName());
				} else {
					this.typeOfProperty = null;
				}
			} catch (Exception e) {
				throw new RuntimeException("Could not get type of Collection with reflection");
			}
		} else {
			typeOfProperty = null;
		}
		isPartitionKeyColumn = super.isPartitionKeyColumn();
	}

	@Override
	public boolean isEmbedded() {
		return isEmbedded;
	}

	@Override
	public Class getTypeOfProperty() {
		return typeOfProperty;
	}

	@Override
	public boolean isPartitionKeyColumn() {
		return isPartitionKeyColumn;
	}

}
