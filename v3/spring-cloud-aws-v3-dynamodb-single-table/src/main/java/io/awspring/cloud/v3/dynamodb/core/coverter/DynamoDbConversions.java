package io.awspring.cloud.v3.dynamodb.core.coverter;

import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.JodaTimeConverters;
import org.springframework.data.convert.Jsr310Converters;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.util.*;
import java.util.function.Predicate;

public class DynamoDbConversions extends CustomConversions {

	private static final StoreConversions STORE_CONVERSIONS;
	private static final List<Object> STORE_CONVERTERS;

	static {
		List<Object> converters = new ArrayList<>(DynamoDbConverters.getConvertersToRegister());
		STORE_CONVERTERS = Collections.unmodifiableList(converters);

		STORE_CONVERSIONS = StoreConversions.of(SimpleTypeHolder.DEFAULT, STORE_CONVERTERS);
	}

	public DynamoDbConversions(List<?> converters) {
		super(new ConverterConfiguration(STORE_CONVERSIONS, converters));
	}
	
	public DynamoDbConversions(ConverterConfiguration converterConfiguration) {
		super(converterConfiguration);
	}

	public DynamoDbConversions(StoreConversions storeConversions, Collection<?> converters) {
		super(storeConversions, converters);
	}

	DynamoDbConversions() {
		this(Collections.emptyList());
	}
}
