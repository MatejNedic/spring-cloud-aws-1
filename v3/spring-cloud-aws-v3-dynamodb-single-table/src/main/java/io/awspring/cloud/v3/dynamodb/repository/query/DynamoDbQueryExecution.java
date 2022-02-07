package io.awspring.cloud.v3.dynamodb.repository.query;

import io.awspring.cloud.v3.dynamodb.core.DynamoDbOperations;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface DynamoDbQueryExecution {

	Object execute(String statement, Class<?> type);


	final class AttributeValueReturnQuery implements DynamoDbQueryExecution {

		private final DynamoDbOperations operations;

		AttributeValueReturnQuery(DynamoDbOperations operations) {
			this.operations = operations;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.cassandra.repository.query.CassandraQueryExecution#execute(java.lang.String, java.lang.Class)
		 */
		@Override
		public Object execute(String statement, Class<?> type) {
			return operations.execute(statement);
		}
	}

	final class ResultProcessingExecution implements DynamoDbQueryExecution {

		private final DynamoDbQueryExecution delegate;

		ResultProcessingExecution(DynamoDbQueryExecution delegate) {
			this.delegate = delegate;
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.cassandra.repository.query.CassandraQueryExecution#execute(java.lang.String, java.lang.Class)
		 */
		@Nullable
		@Override
		public Object execute(String statement, Class<?> type) {

			return delegate.execute(statement, type);
		}
	}


}
