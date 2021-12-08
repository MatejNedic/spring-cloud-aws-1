import io.awspring.cloud.v3.dynamodb.core.coverter.MappingDynamoDbConverter;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbMappingContext;
import io.awspring.cloud.v3.dynamodb.core.mapping.PartitionKey;
import io.awspring.cloud.v3.dynamodb.core.mapping.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

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
		TestClass testClassToBeInserted = new TestClass("testID", "Value");
		Map<String, AttributeValue> mapToBeChecked = new HashMap<>();
		mappingDynamoDbConverter.write(testClassToBeInserted, mapToBeChecked);
		assertThat(mapToBeChecked.get("id").s()).isEqualTo("testID");
		assertThat(mapToBeChecked.get("value").s()).isEqualTo("Value");
	}


	@Table
	public class TestClass {

		@PartitionKey
		private String id;

		private String value;

		public TestClass() {
		}

		public TestClass(String id, String value) {
			this.id = id;
			this.value = value;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
