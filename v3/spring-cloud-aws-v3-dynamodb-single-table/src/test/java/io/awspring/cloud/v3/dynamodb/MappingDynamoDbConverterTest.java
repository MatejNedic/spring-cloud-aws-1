package io.awspring.cloud.v3.dynamodb;

import io.awspring.cloud.v3.dynamodb.core.coverter.MappingDynamoDbConverter;
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
		TestClass testClassToBeInserted = new TestClass("testID", testDate, Arrays.asList("test1", "test2"), Collections.singletonList(new TelephoneNumber("099")));
		Map<String, AttributeValue> mapToBeChecked = new HashMap<>();
		mappingDynamoDbConverter.write(testClassToBeInserted, mapToBeChecked);
		assertThat(mapToBeChecked.get("id").s()).isEqualTo("testID");
		assertThat(mapToBeChecked.get("value").s()).isEqualTo(testDate.toString());
		assertThat(mapToBeChecked.get("myList").l().size()).isEqualTo(2);
	}


	@Table("SomeTableName")
	public static class TestClass {

		@PartitionKey
		private String id;

		private LocalDate value;

		private List<String> myList;

		private List<TelephoneNumber> telephoneNumber;

		public TestClass() {
		}

		public TestClass(String id, LocalDate value) {
			this.id = id;
			this.value = value;
		}

		public TestClass(String id, LocalDate value, List<String> myList) {
			this.myList = myList;
			this.id = id;
			this.value = value;
		}

		public TestClass(String id, LocalDate value, List<String> myList, List<TelephoneNumber> telephoneNumber) {
			this.telephoneNumber = telephoneNumber;
			this.myList = myList;
			this.id = id;
			this.value = value;
		}

		public List<TelephoneNumber> getTelephoneNumber() {
			return telephoneNumber;
		}

		public void setTelephoneNumber(List<TelephoneNumber> telephoneNumber) {
			this.telephoneNumber = telephoneNumber;
		}

		public List<String> getMyList() {
			return myList;
		}

		public void setMyList(List<String> myList) {
			this.myList = myList;
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

	public static class TelephoneNumber {
		private String telephone;

		public TelephoneNumber() {
		}

		public TelephoneNumber(String telephone) {
			this.telephone = telephone;
		}

		public String getTelephone() {
			return telephone;
		}

		public void setTelephone(String telephone) {
			this.telephone = telephone;
		}
	}

}
