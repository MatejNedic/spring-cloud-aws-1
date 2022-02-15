package io.awspring.cloud.v3.dynamodb.request;

import java.util.Map;

public class QueryRequest extends AWSDynamoDbParameters {
	Map<String,Object> exclusiveStartKey;
	String filterExpression;
	String indexName;
	String 	keyConditionExpression;
	Boolean scanIndexForward;

	public Map<String, Object> getExclusiveStartKey() {
		return exclusiveStartKey;
	}

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


	public static final class Builder {
		Map<String,Object> exclusiveStartKey;
		String filterExpression;
		String indexName;
		String 	keyConditionExpression;
		Boolean scanIndexForward;
		private Map<String, String> expressionAttributeNames;
		private Map<String, Object> expressionAttributeValues;

		private Builder() {
		}

		public static Builder aQueryRequest() {
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

		public Builder withExclusiveStartKey(Map<String, Object> exclusiveStartKey) {
			this.exclusiveStartKey = exclusiveStartKey;
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

		public QueryRequest build() {
			QueryRequest queryRequest = new QueryRequest();
			queryRequest.exclusiveStartKey = this.exclusiveStartKey;
			queryRequest.indexName = this.indexName;
			queryRequest.expressionAttributeValues = this.expressionAttributeValues;
			queryRequest.filterExpression = this.filterExpression;
			queryRequest.scanIndexForward = this.scanIndexForward;
			queryRequest.expressionAttributeNames = this.expressionAttributeNames;
			queryRequest.keyConditionExpression = this.keyConditionExpression;
			return queryRequest;
		}
	}
}
