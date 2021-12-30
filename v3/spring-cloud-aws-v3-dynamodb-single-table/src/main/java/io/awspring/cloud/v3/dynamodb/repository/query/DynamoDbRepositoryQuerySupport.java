package io.awspring.cloud.v3.dynamodb.repository.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.stereotype.Repository;

public class DynamoDbRepositoryQuerySupport implements RepositoryQuery {
	protected final Log log = LogFactory.getLog(getClass());

	private final DynamoDbQueryMethod queryMethod;

	private final EntityInstantiators instantiators;

	private final QueryStatementCreator queryStatementCreator;
}
