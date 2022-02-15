package io.awspring.cloud.v3.dynamodb.request;

import java.util.Map;

public class DynamoDBQueryRequest extends AWSDynamoDbParameters {
	String filterExpression;
	String indexName;
	String 	keyConditionExpression;
	Boolean scanIndexForward = Boolean.FALSE;
	Boolean consistentRead = Boolean.FALSE;

	public String getFilterExpression() {
		return filterExpression;
	}

	public String getIndexName() {
		return indexName;
	}

	public String getKeyConditionExpression() {
		return keyConditionExpression;
	}

	public Boolean getScanIndexForward() {
		return scanIndexForward;
	}

	public Boolean getConsistentRead() {
		return consistentRead;
	}


	public static final class Builder {
		Map<String, String> expressionAttributeNames;
		Map<String, Object> expressionAttributeValues;
		String filterExpression;
		String indexName;
		String 	keyConditionExpression;
		Boolean scanIndexForward = Boolean.FALSE;
		Boolean consistentRead = Boolean.FALSE;

		private Builder() {
		}

		public static Builder aDynamoDBQueryRequest() {
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

		public Builder withFilterExpression(String filterExpression) {
			this.filterExpression = filterExpression;
			return this;
		}

		public Builder withIndexName(String indexName) {
			this.indexName = indexName;
			return this;
		}

		public Builder withKeyConditionExpression(String keyConditionExpression) {
			this.keyConditionExpression = keyConditionExpression;
			return this;
		}

		public Builder withScanIndexForward(Boolean scanIndexForward) {
			this.scanIndexForward = scanIndexForward;
			return this;
		}

		public Builder withConsistentRead(Boolean consistentRead) {
			this.consistentRead = consistentRead;
			return this;
		}

		public DynamoDBQueryRequest build() {
			DynamoDBQueryRequest dynamoDBQueryRequest = new DynamoDBQueryRequest();
			dynamoDBQueryRequest.expressionAttributeNames = this.expressionAttributeNames;
			dynamoDBQueryRequest.scanIndexForward = this.scanIndexForward;
			dynamoDBQueryRequest.keyConditionExpression = this.keyConditionExpression;
			dynamoDBQueryRequest.filterExpression = this.filterExpression;
			dynamoDBQueryRequest.indexName = this.indexName;
			dynamoDBQueryRequest.expressionAttributeValues = this.expressionAttributeValues;
			dynamoDBQueryRequest.consistentRead = this.consistentRead;
			return dynamoDBQueryRequest;
		}
	}
}
