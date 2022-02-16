package io.awspring.cloud.v3.dynamodb.request;

import com.sun.istack.internal.NotNull;

import java.util.Map;

public class DynamoDBUpdateExpressionRequest extends DynamoDBConditionRequest{

	@NotNull
	String updateExpression;

	public String getUpdateExpression() {
		return updateExpression;
	}


	public static final class Builder {
		Map<String, String> expressionAttributeNames;
		Map<String, Object> expressionAttributeValues;
		String conditionExpression;
		String updateExpression;

		private Builder() {
		}

		public static Builder aDynamoDBUpdateExpressionRequest() {
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

		public Builder withUpdateExpression(String updateExpression) {
			this.updateExpression = updateExpression;
			return this;
		}

		public DynamoDBUpdateExpressionRequest build() {
			DynamoDBUpdateExpressionRequest dynamoDBUpdateExpressionRequest = new DynamoDBUpdateExpressionRequest();
			dynamoDBUpdateExpressionRequest.conditionExpression = conditionExpression;
			dynamoDBUpdateExpressionRequest.expressionAttributeValues = this.expressionAttributeValues;
			dynamoDBUpdateExpressionRequest.updateExpression = this.updateExpression;
			dynamoDBUpdateExpressionRequest.expressionAttributeNames = this.expressionAttributeNames;
			return dynamoDBUpdateExpressionRequest;
		}
	}
}
