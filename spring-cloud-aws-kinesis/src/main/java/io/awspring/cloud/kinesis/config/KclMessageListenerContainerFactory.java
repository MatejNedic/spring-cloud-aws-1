/*
 * Copyright 2013-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.awspring.cloud.kinesis.config;

import io.awspring.cloud.kinesis.listener.KclContainerOptions;
import io.awspring.cloud.kinesis.listener.KclMessageListenerContainer;
import io.awspring.cloud.kinesis.listener.ListenerMode;
import io.awspring.cloud.kinesis.listener.MessageListenerContainer;
import io.awspring.cloud.kinesis.listener.adapter.BatchMessagingMessageListenerAdapter;
import io.awspring.cloud.kinesis.listener.adapter.MessagingMessageListenerAdapter;
import io.awspring.cloud.kinesis.listener.errorhandler.ErrorHandler;
import io.awspring.cloud.kinesis.operations.KinesisOperations;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;

/**
 * @author Matej Nedic
 * @since 4.2.0
 */
public class KclMessageListenerContainerFactory implements MessageListenerContainerFactory {

	private final KinesisAsyncClient kinesisClient;

	private final DynamoDbAsyncClient dynamoDbClient;

	private final CloudWatchAsyncClient cloudWatchClient;

	@Nullable
	private Consumer<KclContainerOptions.Builder> optionsConsumer;

	@Nullable
	private ErrorHandler errorHandler;

	@Nullable
	private KinesisOperations kinesisOperations;

	public KclMessageListenerContainerFactory(KinesisAsyncClient kinesisClient, DynamoDbAsyncClient dynamoDbClient,
			CloudWatchAsyncClient cloudWatchClient) {
		Assert.notNull(kinesisClient, "kinesisClient must not be null");
		Assert.notNull(dynamoDbClient, "dynamoDbClient must not be null");
		Assert.notNull(cloudWatchClient, "cloudWatchClient must not be null");
		this.kinesisClient = kinesisClient;
		this.dynamoDbClient = dynamoDbClient;
		this.cloudWatchClient = cloudWatchClient;
	}

	public void configure(Consumer<KclContainerOptions.Builder> optionsConsumer) {
		this.optionsConsumer = optionsConsumer;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public void setKinesisOperations(KinesisOperations kinesisOperations) {
		this.kinesisOperations = kinesisOperations;
	}

	@Override
	public MessageListenerContainer createContainer(KclListenerEndpoint endpoint) {
		Assert.notNull(endpoint, "endpoint must not be null");
		KclContainerOptions.Builder optionsBuilder = KclContainerOptions.builder();
		if (this.optionsConsumer != null) {
			this.optionsConsumer.accept(optionsBuilder);
		}
		if (endpoint.getRetrievalMode() != null) {
			optionsBuilder.retrievalMode(endpoint.getRetrievalMode());
		}
		KclMessageListenerContainer container = new KclMessageListenerContainer(this.kinesisClient, this.dynamoDbClient,
				this.cloudWatchClient, endpoint.getStreamName(), endpoint.getApplicationName(), optionsBuilder.build());
		container.setId(endpoint.getId());
		if (endpoint.getCheckpointMode() != null) {
			container.setCheckpointMode(endpoint.getCheckpointMode());
		}
		if (this.errorHandler != null) {
			container.setErrorHandler(this.errorHandler);
		}
		configureListener(container, endpoint);
		return container;
	}

	private void configureListener(KclMessageListenerContainer container, KclListenerEndpoint endpoint) {
		InvocableHandlerMethod handlerMethod = endpoint.getHandlerMethodFactory()
				.createInvocableHandlerMethod(endpoint.getBean(), endpoint.getMethod());
		String replyStream = endpoint.getReplyStream();
		if (isBatchListener(endpoint.getMethod())) {
			container.setListenerMode(ListenerMode.BATCH);
			container.setBatchMessageListener(
					new BatchMessagingMessageListenerAdapter(handlerMethod, this.kinesisOperations, replyStream));
		}
		else {
			container.setListenerMode(ListenerMode.SINGLE_RECORD);
			container.setMessageListener(
					new MessagingMessageListenerAdapter(handlerMethod, this.kinesisOperations, replyStream));
		}
	}

	private static boolean isBatchListener(Method method) {
		for (Parameter parameter : method.getParameters()) {
			if (Collection.class.isAssignableFrom(parameter.getType()) && !isHeaderParameter(parameter)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isHeaderParameter(Parameter parameter) {
		return parameter.isAnnotationPresent(Header.class) || parameter.isAnnotationPresent(Headers.class);
	}

}
