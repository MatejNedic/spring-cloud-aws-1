package io.awspring.cloud.v3.dynamodb.core.mapping;

import org.springframework.data.mapping.MappingException;

public class PartitionKeyClassEntityMetadataVerifier implements DynamoDbPersistentEntityMetadataVerifier {
	@Override
	public void verify(DynamoDbPersistenceEntity<?> entity) throws MappingException {

	}
}
