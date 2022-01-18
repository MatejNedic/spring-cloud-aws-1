package io.awspring.cloud.v3.dynamodb.repository.query;

import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistenceEntity;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistentProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.util.Assert;

public abstract class DynamoDbRepositoryQuerySupport implements RepositoryQuery {
	protected final Log log = LogFactory.getLog(getClass());

	private final DynamoDbQueryMethod queryMethod;

	private final EntityInstantiators instantiators;

	private final QueryStatementCreator queryStatementCreator;

	public DynamoDbRepositoryQuerySupport(DynamoDbQueryMethod queryMethod,
										  MappingContext<? extends DynamoDbPersistenceEntity<?>, DynamoDbPersistentProperty> mappingContext) {

		Assert.notNull(queryMethod, "DynamoDbQueryMethod must not be null");
		Assert.notNull(mappingContext, "DynamoDbMappingContext must not be null");

		this.queryMethod = queryMethod;
		this.instantiators = new EntityInstantiators();
		this.queryStatementCreator = new QueryStatementCreator(queryMethod, mappingContext);
	}

	@Override
	public DynamoDbQueryMethod getQueryMethod() {
		return this.queryMethod;
	}

	protected EntityInstantiators getEntityInstantiators() {
		return this.instantiators;
	}

	protected QueryStatementCreator getQueryStatementCreator() {
		return this.queryStatementCreator;
	}

	class DynamoDbReturnedType {

		private final ReturnedType returnedType;
		private final CustomConversions customConversions;

		DynamoDbReturnedType(ReturnedType returnedType, CustomConversions customConversions) {
			this.returnedType = returnedType;
			this.customConversions = customConversions;
		}


		Class<?> getDomainType() {
			return this.returnedType.getDomainType();
		}

		Class<?> getReturnedType() {
			return this.returnedType.getReturnedType();
		}
	}
}
