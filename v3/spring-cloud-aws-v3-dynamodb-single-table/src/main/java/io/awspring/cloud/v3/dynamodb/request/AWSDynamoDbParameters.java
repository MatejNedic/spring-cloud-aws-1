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


	public static final class Builder {
		Map<String, String> expressionAttributeNames;
		Map<String, Object> expressionAttributeValues;

		private Builder() {
		}

		public static Builder anAWSDynamoDbParameters() {
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

		public AWSDynamoDbParameters build() {
			AWSDynamoDbParameters aWSDynamoDbParameters = new AWSDynamoDbParameters();
			aWSDynamoDbParameters.expressionAttributeValues = this.expressionAttributeValues;
			aWSDynamoDbParameters.expressionAttributeNames = this.expressionAttributeNames;
			return aWSDynamoDbParameters;
		}
	}
}
