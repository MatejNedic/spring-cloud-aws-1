package io.awspring.cloud.v3.dynamodb;

import io.awspring.cloud.v3.dynamodb.core.DynamoDbTemplate;
import io.awspring.cloud.v3.dynamodb.core.EntityReadResult;
import io.awspring.cloud.v3.dynamodb.core.coverter.MappingDynamoDbConverter;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbMappingContext;
import io.awspring.cloud.v3.dynamodb.core.mapping.events.DynamoDbBeforeSaveCallback;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamoDbTemplateTest extends LocalStackTestContainer {

	private DynamoDbTemplate dynamoDbTemplate;
	private DynamoDbClient dynamoDbClient;
	private MappingDynamoDbConverter mappingDynamoDbConverter;

	{	
		DynamoDbMappingContext mappingContext = new DynamoDbMappingContext();
		mappingDynamoDbConverter = new MappingDynamoDbConverter(mappingContext);
		mappingDynamoDbConverter.afterPropertiesSet();
		dynamoDbClient = DynamoDbClient.builder().region(Region.of(localstack.getRegion()))
			.endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB)).build();
		EntityCallbacks callbacks = EntityCallbacks.create();
		callbacks.addEntityCallback((DynamoDbBeforeSaveCallback<Object>) (entity, tableName) -> {
			assertThat(tableName).isNotNull();
			return entity;
		});
		dynamoDbTemplate = new DynamoDbTemplate(dynamoDbClient, mappingDynamoDbConverter);
		dynamoDbTemplate.setEntityCallbacks(callbacks);

		KeySchemaElement idKey = KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build();
		AttributeDefinition id = AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build();
		CreateTableRequest createTableRequest = CreateTableRequest.builder().tableName("SomeTableName").attributeDefinitions(id).keySchema(idKey).provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(10L).writeCapacityUnits(10L).build()).build();
		dynamoDbClient.createTable(createTableRequest);
	}

	@Test
	void insertShouldInsertEntity() {
		LocalDate testDate = LocalDate.now();
		MappingDynamoDbConverterTest.TestClass testClassToBeInserted = new MappingDynamoDbConverterTest.TestClass("testID", testDate);

		dynamoDbTemplate.save(testClassToBeInserted);

		Map keyToFetch = new HashMap();
		keyToFetch.put("id", AttributeValue.builder().s("testID").build());
		Map<String, AttributeValue> attributeValueHashMap = dynamoDbClient.getItem(GetItemRequest.builder().key(keyToFetch).tableName("SomeTableName").build()).item();

		Assert.assertEquals(attributeValueHashMap.get("id").s(), testClassToBeInserted.getId());
		Assert.assertEquals(LocalDate.parse(attributeValueHashMap.get("value").s()), testClassToBeInserted.getValue());
	}

	@Test
	void insertShouldInsertEntityNullFields() {
		MappingDynamoDbConverterTest.TestClass testClassToBeInserted = new MappingDynamoDbConverterTest.TestClass("anotherId", null);

		dynamoDbTemplate.save(testClassToBeInserted);

		Map keyToFetch = new HashMap();
		keyToFetch.put("id", AttributeValue.builder().s("anotherId").build());
		Map<String, AttributeValue> attributeValueHashMap = dynamoDbClient.getItem(GetItemRequest.builder().key(keyToFetch).tableName("SomeTableName").build()).item();

		Assert.assertEquals(attributeValueHashMap.get("id").s(), testClassToBeInserted.getId());
		Assert.assertTrue(attributeValueHashMap.get("value").nul());
	}


	@Test
	void insertThenDelete() {
		LocalDate testDate = LocalDate.now();
		MappingDynamoDbConverterTest.TestClass testClassToBeInserted = new MappingDynamoDbConverterTest.TestClass("testID2", testDate);

		dynamoDbTemplate.save(testClassToBeInserted);

		Map keyToFetch = new HashMap();
		keyToFetch.put("id", AttributeValue.builder().s("testID2").build());
		Map<String, AttributeValue> attributeValueHashMap = dynamoDbClient.getItem(GetItemRequest.builder().key(keyToFetch).tableName("SomeTableName").build()).item();

		Assert.assertEquals(attributeValueHashMap.get("id").s(), testClassToBeInserted.getId());
		Assert.assertEquals(LocalDate.parse(attributeValueHashMap.get("value").s()), testClassToBeInserted.getValue());

		dynamoDbTemplate.delete(testClassToBeInserted);

		attributeValueHashMap = dynamoDbClient.getItem(GetItemRequest.builder().key(keyToFetch).tableName("SomeTableName").build()).item();
		Assert.assertEquals(attributeValueHashMap.size(), 0L);
	}


	@Test
	void insertThenGet() {
		LocalDate testDate = LocalDate.now();
		MappingDynamoDbConverterTest.TestClass testClassToBeInserted = new MappingDynamoDbConverterTest.TestClass("testID3", testDate, Arrays.asList("test1", "test2"), Arrays.asList(new MappingDynamoDbConverterTest.TelephoneNumber("099"), new MappingDynamoDbConverterTest.TelephoneNumber("095")));

		dynamoDbTemplate.save(testClassToBeInserted);
		MappingDynamoDbConverterTest.TestClass readClass = dynamoDbTemplate.getEntityByKey(testClassToBeInserted.getId(), MappingDynamoDbConverterTest.TestClass.class);

		Assert.assertEquals(readClass.getId(), testClassToBeInserted.getId());
		Assert.assertEquals(readClass.getValue(), testClassToBeInserted.getValue());
		Assert.assertEquals(readClass.getMyList(), Arrays.asList("test1", "test2"));
		Assert.assertEquals(readClass.getTelephoneNumber().size(), 2);
	}



	@Test
	void insertUpdateThenGet() {
		LocalDate testDate = LocalDate.now();
		MappingDynamoDbConverterTest.TestClass testClassToBeInserted = new MappingDynamoDbConverterTest.TestClass("testID4", testDate);

		dynamoDbTemplate.save(testClassToBeInserted);

		LocalDate newDate = testDate.plusDays(1);
		testClassToBeInserted.setValue(newDate);
		dynamoDbTemplate.update(testClassToBeInserted);

		Map keyToFetch = new HashMap();
		keyToFetch.put("id", AttributeValue.builder().s("testID4").build());
		Map<String, AttributeValue> attributeValueHashMap = dynamoDbClient.getItem(GetItemRequest.builder().key(keyToFetch).tableName("SomeTableName").build()).item();


		Assert.assertEquals(attributeValueHashMap.get("id").s(), testClassToBeInserted.getId());
		Assert.assertEquals(LocalDate.parse(attributeValueHashMap.get("value").s()), newDate);
	}


	@Test
	void insertAndThenExecute() {
		LocalDate testDate = LocalDate.now();
		MappingDynamoDbConverterTest.TestClass testClassToBeInserted = new MappingDynamoDbConverterTest.TestClass("randomId", testDate);

		dynamoDbTemplate.save(testClassToBeInserted);

		EntityReadResult<List<MappingDynamoDbConverterTest.TestClass>> list = dynamoDbTemplate.executeStatement("Select * from SomeTableName", null, MappingDynamoDbConverterTest.TestClass.class);
		assertThat(list.getEntity().size()).isEqualTo(1L);
		assertThat(list.getEntity().get(0).getId()).isEqualTo(testClassToBeInserted.getId());
		assertThat(list.getEntity().get(0).getMyList()).isEqualTo(testClassToBeInserted.getMyList());
		assertThat(list.getEntity().get(0).getTelephoneNumber()).isEqualTo(testClassToBeInserted.getTelephoneNumber());
		assertThat(list.getEntity().get(0).getValue()).isEqualTo(testClassToBeInserted.getValue());
	}



}
