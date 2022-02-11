package io.awspring.cloud.v3.dynamodb.core.coverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.awspring.cloud.v3.dynamodb.core.mapping.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.data.mapping.model.EntityInstantiator;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class MappingDynamoDbConverter extends AbstractDynamoDbConverter implements ApplicationContextAware, BeanClassLoaderAware {
	private @Nullable
	ClassLoader beanClassLoader;

	//There should be option to change mapper in a future.
	private final static ObjectMapper objectMapper = new ObjectMapper();
	private final DynamoDbMappingContext mappingContext;


	public MappingDynamoDbConverter(DynamoDbMappingContext mappingContext) {
		super(newConversionService());
		DynamoDbConversions conversions = new DynamoDbConversions(Collections.emptyList());
		this.mappingContext = mappingContext;
		this.setCustomConversions(conversions);
	}

	private static ConversionService newConversionService() {
		return new DefaultConversionService();
	}

	@Override
	public Object getId(Object object, DynamoDbPersistenceEntity<?> entity) {

		Assert.notNull(object, "Object instance must not be null");
		Assert.notNull(entity, "DynamoDbPersistenceEntity must not be null");

		ConvertingPropertyAccessor<?> propertyAccessor = newConvertingPropertyAccessor(object, entity);

		DynamoDbPersistentProperty idProperty = entity.getIdProperty();

		Assert.notNull(idProperty, "ID property cannot be null.");
		return propertyAccessor.getProperty(idProperty);
	}


	@Override
	public void write(Object obj, Map<String, AttributeValue> items, DynamoDbPersistenceEntity<?> persistentEntity) {

		Assert.notNull(obj, "Value must not be null");

		if (persistentEntity == null) {
			throw new MappingException("No mapping metadata found for " + persistentEntity.getClass());
		}

		for (DynamoDbPersistentProperty property : persistentEntity) {
			if (property.isEntity()) {
				try {
					items.put(property.getColumnName(), AttributeValue.builder().s(objectMapper.writeValueAsString(newConvertingPropertyAccessor(obj, persistentEntity).getProperty(property))).build());
				} catch (JsonProcessingException e) {
					throw new RuntimeException("Could not transform entity to Json format");
				}
			} else {
				writeInternal(newConvertingPropertyAccessor(obj, persistentEntity), property, items);
			}
		}
	}

	@Nullable
	private void writeInternal(ConvertingPropertyAccessor<?> convertingPropertyAccessor, DynamoDbPersistentProperty property, Map<String, AttributeValue> attributeValueMap) {
		attributeValueMap.put(property.getColumnName(), toAttributeValue(convertingPropertyAccessor.getProperty(property)));
	}

	@Override
	public void delete(Object objectToDelete, Map<String, AttributeValue> keys, DynamoDbPersistenceEntity<?> persistenceEntity) {
		fetchKeysAndPopulate(objectToDelete, keys, persistenceEntity);
	}

	@Override
	public void findByKey(Object key, Map<String, AttributeValue> keys, DynamoDbPersistenceEntity<?> persistenceEntity) {
		DynamoDbPersistentProperty persistentProperty = persistenceEntity.getPersistentProperty(PartitionKey.class);
		keys.put(persistentProperty.getColumnName(), toAttributeValue(key));
	}

	@Override
	public void findByKeys(Map<String, Object> keysForLookUp, Map<String, AttributeValue> keys, DynamoDbPersistenceEntity<?> persistenceEntity) {
		keysForLookUp.forEach((k, v) -> {
			keys.put(k, toAttributeValue(v));
		});
	}

	@Override
	public void update(Object objectToUpdate, Map<String, AttributeValue> keys, DynamoDbPersistenceEntity<?> entity, Map<String, AttributeValueUpdate> attributeUpdates) {
		fetchKeysAndPopulate(objectToUpdate, keys, entity);
		for (DynamoDbPersistentProperty property : entity) {
			if (!property.isIdProperty() && !property.isRangeKey()) {
				attributeUpdates.put(property.getColumnName(), AttributeValueUpdate.builder().value(toAttributeValue(newConvertingPropertyAccessor(objectToUpdate, entity).getProperty(property))).build());
			}
		}

	}

	@Override
	public <R> R read(Class<R> type, Map<String, AttributeValue> source) {

		DynamoDbPersistenceEntity<R> entity = (DynamoDbPersistenceEntity<R>) getMappingContext()
			.getPersistentEntity(type);
		/*
		If we gonna call constructor instead of mapping without.
		PreferredConstructor<R, DynamoDbPersistentProperty> persistenceConstructor = entity.getPersistenceConstructor();
		 */
		EntityInstantiator instantiator = this.instantiators.getInstantiatorFor(entity);
		ParameterValueProvider<DynamoDbPersistentProperty> provider = NoOpParameterValueProvider.INSTANCE;
		R instance = instantiator.createInstance(entity, provider);
		ConvertingPropertyAccessor<R> propertyAccessor = newConvertingPropertyAccessor(instance, entity);

		for (DynamoDbPersistentProperty property : entity) {
			if (property.isEntity() && property.getType() == List.class) {
				propertyAccessor.setProperty(property, fromAttributeValue(source.get(property.getColumnName()), property.getTypeOfProperty(), property.isEntity(), true));
			} else {
				propertyAccessor.setProperty(property, fromAttributeValue(source.get(property.getColumnName()), property.getType(), property.isEntity(), false));
			}
		}
		return instance;
	}

	private <S> ConvertingPropertyAccessor<S> newConvertingPropertyAccessor(S source,
																			DynamoDbPersistenceEntity<?> entity) {

		PersistentPropertyAccessor<S> propertyAccessor = source instanceof PersistentPropertyAccessor
			? (PersistentPropertyAccessor<S>) source
			: entity.getPropertyAccessor(source);

		return new ConvertingPropertyAccessor<>(propertyAccessor, getConversionService());
	}

	@Override
	public void write(Object source, Map<String, AttributeValue> sink) {
		Assert.notNull(source, "Value must not be null");

		Class<?> beanClassLoaderClass = transformClassToBeanClassLoaderClass(source.getClass());

		DynamoDbPersistenceEntity<?> entity = getMappingContext().getRequiredPersistentEntity(beanClassLoaderClass);

		write(source, sink, entity);
	}

	private void fetchKeysAndPopulate(Object toBeUsed, Map<String, AttributeValue> keys, DynamoDbPersistenceEntity<?> entity) {
		DynamoDbPersistentProperty persistentProperty = entity.getPersistentProperty(PartitionKey.class);
		ConvertingPropertyAccessor convertingPropertyAccessor = newConvertingPropertyAccessor(toBeUsed, entity);
		keys.put(persistentProperty.getColumnName(), toAttributeValue(convertingPropertyAccessor.getProperty(persistentProperty)));

		Iterable<DynamoDbPersistentProperty> persistentProperties = entity.getPersistentProperties(RangeKey.class);
		persistentProperties.forEach(rangeProperty -> {
			keys.put(rangeProperty.getColumnName(), toAttributeValue(convertingPropertyAccessor.getProperty(rangeProperty)));
		});
	}

	@Override
	public AttributeValue convertToDynamoDbType(Object obj, DynamoDbPersistenceEntity<?> entity) {
		return 	toAttributeValue(obj);
	}

	@Override
	public MappingContext<? extends DynamoDbPersistenceEntity<?>, DynamoDbPersistentProperty> getMappingContext() {
		return this.mappingContext;
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

	}

	private <T> Class<T> transformClassToBeanClassLoaderClass(Class<T> entity) {

		try {
			return (Class<T>) ClassUtils.forName(entity.getName(), this.beanClassLoader);
		} catch (ClassNotFoundException | LinkageError ignore) {
			return entity;
		}
	}


	public AttributeValue toAttributeValue(Object value) {
		AttributeValue.Builder resultBuilder = AttributeValue.builder();
		if (value == null) {
			return resultBuilder.nul(Boolean.TRUE).build();
		} else if (value instanceof Set) {
			Set<Object> set = (Set<Object>) value;
			if (set.size() == 0) {
				resultBuilder.ss(new ArrayList<>());
				return resultBuilder.build();
			}
			Object element = set.iterator().next();
			return conversionService.convert(element, AttributeValue.class);
		} else if (value instanceof List) {
			List<Object> in = (List<Object>) value;
			List<AttributeValue> out = new ArrayList<AttributeValue>();
			for (Object v : in) {
				out.add(toAttributeValue(v));
			}
			resultBuilder.l(out);
		} else if (value instanceof Map) {
			Map<String, Object> in = (Map<String, Object>) value;
			Map<String, AttributeValue> attrs = new HashMap<>();
			for (Map.Entry<String, Object> e : in.entrySet()) {
				attrs.put(e.getKey(), toAttributeValue(e.getValue()));
			}
			resultBuilder.m(attrs);
		} else {
			return this.conversionService.convert(value, AttributeValue.class);
		}
		return resultBuilder.build();
	}

	public <T> T fromAttributeValue(AttributeValue value, Class clazz, boolean entity, boolean isList) {
		if (value == null || value.nul() != null) {
			return null;
		} else if (entity) {
			try {
				if (isList) {
					JavaType type = objectMapper.getTypeFactory().constructParametricType(List.class, clazz);
					return objectMapper.readValue(value.s(), type);
				} else {
					return (T) objectMapper.readValue(value.s(), clazz);
				}
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Could not transform json to class of type" + clazz);
			}
		} else if (value.hasL()) {
			List<AttributeValue> toBeConverted = value.l();
			List<T> out = new ArrayList<T>();
			for (AttributeValue v : toBeConverted) {
				Class clazzToBeDecided = typeOfAttributeValueClazz(v);
				out.add(fromAttributeValue(v, clazzToBeDecided, false, false));
			}
			return (T) out;
		} else if (value.hasSs()) {
			return (T) value.ss();
		} else if (value.hasNs()) {
			return (T) value.ns();
		} else if (value.hasM()) {
			Map<String, AttributeValue> toBeConverted = value.m();
			Map<String, T> attrs = new HashMap<>();
			for (Map.Entry<String, AttributeValue> e : toBeConverted.entrySet()) {
				attrs.put(e.getKey(), fromAttributeValue(e.getValue(), typeOfAttributeValueClazz(e.getValue()), false, false));
			}
			return (T) attrs;
		}
		return (T) this.conversionService.convert(value, clazz);
	}

	public Class typeOfAttributeValueClazz(AttributeValue value) {
		if (value.s() != null) {
			return String.class;
		} else if (value.n() != null) {
			return Number.class;
		} else if (value.bool() != null) {
			return Boolean.class;
		} else if (value.l() != null) {
			return List.class;
		} else if (value.m() != null) {
			return Map.class;
		} else if (value.b() != null) {
			return Byte.class;
		} else if (value.ns() != null) {
			return List.class;
		} else if (value.ss() != null) {
			return List.class;
		} else if (value.bs() != null) {
			return List.class;
		}
		return null;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	enum NoOpParameterValueProvider implements ParameterValueProvider<DynamoDbPersistentProperty> {

		INSTANCE;

		@Override
		public <T> T getParameterValue(PreferredConstructor.Parameter<T, DynamoDbPersistentProperty> parameter) {
			return null;
		}
	}
}
