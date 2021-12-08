package io.awspring.cloud.v3.dynamodb.core.coverter;

import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbMappingContext;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistentProperty;
import io.awspring.cloud.v3.dynamodb.core.mapping.PartitionKey;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.data.mapping.model.EntityInstantiator;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class MappingDynamoDbConverter extends AbstractDynamoDbConverter implements ApplicationContextAware, BeanClassLoaderAware {
	private @Nullable ClassLoader beanClassLoader;

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
		return null;
	}


	@Override
	public void write(Object obj, Map<String, AttributeValue> items, DynamoDbPersistenceEntity<?> persistentEntity) {

		Assert.notNull(obj, "Value must not be null");

		if (persistentEntity == null) {
			throw new MappingException("No mapping metadata found for " + persistentEntity.getClass());
		}

		for (DynamoDbPersistentProperty property : persistentEntity) {
			writeInternal(newConvertingPropertyAccessor(obj, persistentEntity), property, items);
		}
	}

	@Override
	public void delete(Object objectToDelete, Map<String, AttributeValue> keys, DynamoDbPersistenceEntity<?> persistenceEntity) {
		DynamoDbPersistentProperty persistentProperty = persistenceEntity.getPersistentProperty(PartitionKey.class);
		ConvertingPropertyAccessor convertingPropertyAccessor = newConvertingPropertyAccessor(objectToDelete, persistenceEntity);
		keys.put(persistentProperty.getColumnName(), toAttributeValue(convertingPropertyAccessor.getProperty(persistentProperty)));
	}

	@Nullable
	private void writeInternal(ConvertingPropertyAccessor<?> convertingPropertyAccessor, DynamoDbPersistentProperty property, Map<String, AttributeValue> attributeValueMap) {
		attributeValueMap.put(property.getColumnName(), toAttributeValue(convertingPropertyAccessor.getProperty(property)));
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
			propertyAccessor.setProperty(property, getConversionService().convert(source.get(property.getColumnName()), property.getType()));

		}
		return null;
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

	@Override
	public Object convertToDynamoDbType(Object obj, DynamoDbPersistenceEntity<?> entity) {
		Map<String, AttributeValue> attributeValueMap = new HashMap<>();
		write(obj, attributeValueMap ,entity);
		return attributeValueMap;
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
	public static AttributeValue toAttributeValue(Object value) {
		AttributeValue.Builder resultBuilder = AttributeValue.builder();
		if (value == null) {
			return resultBuilder.nul(Boolean.TRUE).build();
		} else if (value instanceof Boolean) {
			return resultBuilder.bool((Boolean) value).build();
		} else if (value instanceof String) {
			return resultBuilder.s((String) value).build();
		} else if (value instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal) value;
			return resultBuilder.n(bd.toPlainString()).build();
		} else if (value instanceof Number) {
			return resultBuilder.n(value.toString()).build();
		} else if (value instanceof byte[]) {
			return resultBuilder.b(SdkBytes.fromByteArray((byte[]) value)).build();
		} else if (value instanceof ByteBuffer) {
			return resultBuilder.b(SdkBytes.fromByteBuffer((ByteBuffer) value)).build();
		} else if (value instanceof Set) {
			@SuppressWarnings("unchecked")
			Set<Object> set = (Set<Object>) value;
			if (set.size() == 0) {
				resultBuilder.ss(new ArrayList<>());
				return resultBuilder.build();
			}
			Object element = set.iterator().next();
			if (element instanceof String) {
				@SuppressWarnings("unchecked")
				Set<String> ss = (Set<String>) value;
				resultBuilder.ss(new ArrayList<String>(ss));
			} else if (element instanceof Number) {
				@SuppressWarnings("unchecked")
				Set<Number> in = (Set<Number>) value;
				List<String> out = new ArrayList<String>(set.size());
				for (Number n : in) {
					BigDecimal bd = toBigDecimal(n);
					out.add(bd.toPlainString());
				}
				resultBuilder.ns(out);
			} else if (element instanceof byte[]) {
				@SuppressWarnings("unchecked")
				Set<byte[]> in = (Set<byte[]>) value;
				List<SdkBytes> out = new ArrayList<>(set.size());
				for (byte[] buf : in) {
					out.add(SdkBytes.fromByteArray(buf));
				}
				resultBuilder.bs(out);
			} else if (element instanceof ByteBuffer) {
				@SuppressWarnings("unchecked")
				Set<ByteBuffer> in = (Set<ByteBuffer>) value;
				List<SdkBytes> out = new ArrayList<>(set.size());
				for (ByteBuffer buf : in) {
					out.add(SdkBytes.fromByteBuffer(buf));
				}
				resultBuilder.bs(out);
			} else {
				throw new UnsupportedOperationException("element type: "
					+ element.getClass());
			}
		} else if (value instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> in = (List<Object>) value;
			List<AttributeValue> out = new ArrayList<AttributeValue>();
			for (Object v : in) {
				out.add(toAttributeValue(v));
			}
			resultBuilder.l(out);
		} else if (value instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> in = (Map<String, Object>) value;
			Map<String, AttributeValue> attrs = new HashMap<>();
			for (Map.Entry<String, Object> e : in.entrySet()) {
				attrs.put(e.getKey(), toAttributeValue(e.getValue()));
			}
			resultBuilder.m(attrs);
		} else {
			throw new UnsupportedOperationException("value type: "
				+ value.getClass());
		}
		return resultBuilder.build();
	}

	public static BigDecimal toBigDecimal(Number n) {
		if (n instanceof BigDecimal) {
			return (BigDecimal) n;
		}
		return new BigDecimal(n.toString());
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
