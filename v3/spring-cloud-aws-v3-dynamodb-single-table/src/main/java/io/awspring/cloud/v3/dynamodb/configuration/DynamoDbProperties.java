package io.awspring.cloud.v3.dynamodb.configuration;


import io.awspring.cloud.v3.autoconfigure.AwsClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the AWS DynamoDB integration
 *
 * @author Matej Nedic
 * @since 3.0.0
 */
@ConfigurationProperties(DynamoDbProperties.CONFIG_PREFIX)
public class DynamoDbProperties extends AwsClientProperties {

	/**
	 * Configuration prefix.
	 */
	public static final String CONFIG_PREFIX = "spring.cloud.aws.dynamoDb";

}
