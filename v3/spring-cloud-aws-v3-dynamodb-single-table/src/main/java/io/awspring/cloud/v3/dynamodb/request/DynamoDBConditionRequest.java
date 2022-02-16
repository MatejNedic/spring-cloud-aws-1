package io.awspring.cloud.v3.dynamodb.request;

import java.util.Map;

public class DynamoDBConditionRequest extends AWSDynamoDbParameters{

	String conditionExpression;

	public String getConditionExpression() {
		return conditionExpression;
	}


	public static final class Builder {
		Map<String, String> expressionAttributeNames;
		Map<String, Object> expressionAttributeValues;
		String conditionExpression;

		private Builder() {
		}

		public static Builder aDynamoDBConditionRequest() {
			return new Builder();
		}

		public Builder withExpressionAttributeNames(Map<String, String> expressionAttributeNames) {
			this.expressionAttributeNames = expressionAttributeNames;
			return this;
		}

		public Builder withExpressionAttributeValues(Map<String, Object> expressionAttributeValues) {
			this.expressionAttributeValues = expressionAttributeValues;
			return this;
		}

		public Builder withConditionExpression(String conditionExpression) {
			this.conditionExpression = conditionExpression;
			return this;
		}

		public DynamoDBConditionRequest build() {
			DynamoDBConditionRequest dynamoDBConditionRequest = new DynamoDBConditionRequest();
			dynamoDBConditionRequest.conditionExpression = conditionExpression;
			dynamoDBConditionRequest.expressionAttributeValues = this.expressionAttributeValues;
			dynamoDBConditionRequest.expressionAttributeNames = this.expressionAttributeNames;
			return dynamoDBConditionRequest;
		}
	}
}
