package io.awspring.cloud.v3.dynamodb;

import io.awspring.cloud.v3.dynamodb.core.coverter.MappingDynamoDbConverter;
import io.awspring.cloud.v3.dynamodb.core.mapping.Column;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbMappingContext;
import io.awspring.cloud.v3.dynamodb.core.mapping.PartitionKey;
import io.awspring.cloud.v3.dynamodb.core.mapping.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class MappingDynamoDbConverterTest {


	private DynamoDbMappingContext mappingContext;
	private MappingDynamoDbConverter mappingDynamoDbConverter;


	@BeforeEach
	void setUp() {
		this.mappingContext = new DynamoDbMappingContext();
		this.mappingDynamoDbConverter = new MappingDynamoDbConverter(mappingContext);
		this.mappingDynamoDbConverter.afterPropertiesSet();
	}

	@Test
	void insertTestClass() {
		LocalDate testDate = LocalDate.now();
		TestClass testClassToBeInserted = new TestClass("testID", testDate);
		Map<String, AttributeValue> mapToBeChecked = new HashMap<>();
		mappingDynamoDbConverter.write(testClassToBeInserted, mapToBeChecked);
		assertThat(mapToBeChecked.get("id").s()).isEqualTo("testID");
		assertThat(mapToBeChecked.get("value").s()).isEqualTo(testDate.toString());
	}


	@Table("test")
	public static class TestClass {

		@PartitionKey
		private String id;

		private LocalDate value;

		public TestClass() {
		}

		public TestClass(String id, LocalDate value) {
			this.id = id;
			this.value = value;
		}


		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public LocalDate getValue() {
			return value;
		}

		public void setValue(LocalDate value) {
			this.value = value;
		}
	}


}
