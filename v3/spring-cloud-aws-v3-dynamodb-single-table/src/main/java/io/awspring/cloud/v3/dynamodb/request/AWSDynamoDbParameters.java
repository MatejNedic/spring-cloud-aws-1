package io.awspring.cloud.v3.dynamodb.request;

import java.util.Map;

public class AWSDynamoDbParameters {

	String conditionExpression;
	Map<String, String> expressionAttributeNames;
	Map<String, Object> expressionAttributeValues;
}
