package io.awspring.cloud.v3.dynamodb.core.coverter;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Wrapper class to contain useful converters for the usage with DynamoDb.
 *
 * @author Matej Nedic
 * @since 3.0
 */
public class DynamoDbConverters {

	static Collection<Object> getConvertersToRegister() {

		List<Object> converters = new ArrayList<>();

		converters.add(StringToMarshallTypeString.INSTANCE);
		converters.add(AttributeValueTypeStringToString.INSTANCE);
		converters.add(AttributeValueToNumber.INSTANCE);
		converters.add(NumberToAttributeValue.INSTANCE);
		converters.add(URLToAttributeValue.INSTANCE);
		converters.add(AttributeValueToURLConverter.INSTANCE);
		return converters;
	}

	enum StringToMarshallTypeString implements Converter<String, AttributeValue> {
		INSTANCE;

		public AttributeValue convert(String source) {
			return AttributeValue.builder().s(source).build();
		}
	}

	enum AttributeValueTypeStringToString implements Converter<AttributeValue, String> {
		INSTANCE;

		public String convert(AttributeValue source) {
			return source.s();
		}
	}

	public enum AttributeValueToNumber
			implements ConverterFactory<AttributeValue, Number> {

		INSTANCE;

		@Override
		public <T extends Number> Converter<AttributeValue, T> getConverter(
				Class<T> targetType) {
			Assert.notNull(targetType, "Target type must not be null");
			return new AttributeValueToNumberConverter<>(targetType);
		}

		private static final class AttributeValueToNumberConverter<T extends Number>
				implements Converter<AttributeValue, T> {

			private final Class<T> targetType;

			AttributeValueToNumberConverter(Class<T> targetType) {
				this.targetType = targetType;
			}

			@Override
			public T convert(AttributeValue source) {

				String object = source.n();

				return (object != null ? NumberUtils.parseNumber(object, this.targetType)
						: null);
			}
		}
	}

	public enum NumberToAttributeValue implements Converter<Number, AttributeValue> {

		INSTANCE;

		public AttributeValue convert(Number source) {
			return AttributeValue.builder().n(source.toString()).build();
		}
	}

	enum URLToAttributeValue implements Converter<URL, AttributeValue> {
		INSTANCE;
		public AttributeValue convert(URL source) {
			return AttributeValue.builder().s(source.toString()).build();
		}
	}

	enum AttributeValueToURLConverter implements Converter<AttributeValue, URL> {
		INSTANCE;

		private static final TypeDescriptor SOURCE = TypeDescriptor
				.valueOf(AttributeValue.class);
		private static final TypeDescriptor TARGET = TypeDescriptor.valueOf(URL.class);

		public URL convert(AttributeValue source) {

			try {
				return new URL(source.s());
			}
			catch (MalformedURLException e) {
				throw new ConversionFailedException(SOURCE, TARGET, source, e);
			}
		}
	}

}
