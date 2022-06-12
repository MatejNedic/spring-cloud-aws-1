/*
 * Copyright 2013-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.awspring.cloud.samples.dynamodb;

import io.awspring.cloud.dynamodb.DynamoDbOperations;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@SpringBootApplication
public class SpringDynamoDbSample {

	private DynamoDbOperations dynamoDbOperations;
	private DynamoDbEnhancedClient dynamoDbEnhancedClient;

	public SpringDynamoDbSample(DynamoDbOperations dynamoDbOperations, DynamoDbEnhancedClient dynamoDbEnhancedClient) {
		this.dynamoDbOperations = dynamoDbOperations;
		this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringDynamoDbSample.class, args);

	}

	@EventListener(ApplicationReadyEvent.class)
	public void sendMessage() {
		dynamoDbEnhancedClient.table("department", TableSchema.fromBean(Department.class)).createTable();
		UUID departmentId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Department department = Department.Builder.aDepartment().withDepartmentId(departmentId).withUserId(userId)
				.withOpeningDate(LocalDate.now()).withEmployeeNumber(10L).build();
		// Saving Department
		dynamoDbOperations.save(department);

		// Get Department for departmentId
		Department departmentLoaded = dynamoDbOperations
				.load(Key.builder().partitionValue(AttributeValue.builder().s(departmentId.toString()).build())
						.sortValue(userId.toString()).build(), Department.class);
		// Print openingDate for example.
		System.out.println(departmentLoaded.getOpeningDate());
		// Query
		PageIterable<Department> departmentPageIterable = dynamoDbOperations.query(
				QueryEnhancedRequest.builder()
						.queryConditional(QueryConditional
								.keyEqualTo(Key.builder().partitionValue(departmentId.toString()).build()))
						.build(),
				Department.class);
		// Print number of items queried.
		System.out.println(departmentPageIterable.items().stream().count());

		// Delete
		dynamoDbOperations.delete(department);
	}
}
