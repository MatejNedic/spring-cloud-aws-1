package io.awspring.cloud.v3.dynamodb.request;

import java.util.Map;

public class AWSDynamoDbParameters {
	Map<String, String> expressionAttributeNames;
	Map<String, Object> expressionAttributeValues;

	public Map<String, String> getExpressionAttributeNames() {
		return expressionAttributeNames;
	}

	public Map<String, Object> getExpressionAttributeValues() {
		return expressionAttributeValues;
	}


}
