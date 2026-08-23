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

import io.awspring.cloud.kinesis.listener.checkpoint.CheckpointMode;
import io.awspring.cloud.kinesis.listener.retrieval.RetrievalMode;
import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.Assert;

/**
 * @author Matej Nedic
 * @since 4.2.0
 */
public class KclListenerEndpoint {

	private final String id;

	private final String streamName;

	private final String applicationName;

	@Nullable
	private final String factoryBeanName;

	private final Object bean;

	private final Method method;

	@Nullable
	private final CheckpointMode checkpointMode;

	@Nullable
	private final RetrievalMode retrievalMode;

	@Nullable
	private final String replyStream;

	@Nullable
	private MessageHandlerMethodFactory handlerMethodFactory;

	private KclListenerEndpoint(Builder builder) {
		Assert.hasText(builder.id, "id must not be empty");
		Assert.hasText(builder.streamName, "streamName must not be empty");
		Assert.hasText(builder.applicationName, "applicationName must not be empty");
		Assert.notNull(builder.bean, "bean must not be null");
		Assert.notNull(builder.method, "method must not be null");
		this.id = builder.id;
		this.streamName = builder.streamName;
		this.applicationName = builder.applicationName;
		this.factoryBeanName = builder.factoryBeanName;
		this.bean = builder.bean;
		this.method = builder.method;
		this.checkpointMode = builder.checkpointMode;
		this.retrievalMode = builder.retrievalMode;
		this.replyStream = builder.replyStream;
	}

	public static Builder builder() {
		return new Builder();
	}

	public String getId() {
		return this.id;
	}

	public String getStreamName() {
		return this.streamName;
	}

	public String getApplicationName() {
		return this.applicationName;
	}

	@Nullable
	public String getFactoryBeanName() {
		return this.factoryBeanName;
	}

	public Object getBean() {
		return this.bean;
	}

	public Method getMethod() {
		return this.method;
	}

	public void setHandlerMethodFactory(MessageHandlerMethodFactory handlerMethodFactory) {
		this.handlerMethodFactory = handlerMethodFactory;
	}

	public MessageHandlerMethodFactory getHandlerMethodFactory() {
		Assert.notNull(this.handlerMethodFactory, "handlerMethodFactory has not been set");
		return this.handlerMethodFactory;
	}

	@Nullable
	public CheckpointMode getCheckpointMode() {
		return this.checkpointMode;
	}

	@Nullable
	public RetrievalMode getRetrievalMode() {
		return this.retrievalMode;
	}

	@Nullable
	public String getReplyStream() {
		return this.replyStream;
	}

	public static final class Builder {

		@Nullable
		private String id;

		@Nullable
		private String streamName;

		@Nullable
		private String applicationName;

		@Nullable
		private String factoryBeanName;

		@Nullable
		private Object bean;

		@Nullable
		private Method method;

		@Nullable
		private CheckpointMode checkpointMode;

		@Nullable
		private RetrievalMode retrievalMode;

		@Nullable
		private String replyStream;

		private Builder() {
		}

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder streamName(String streamName) {
			this.streamName = streamName;
			return this;
		}

		public Builder applicationName(String applicationName) {
			this.applicationName = applicationName;
			return this;
		}

		public Builder factoryBeanName(@Nullable String factoryBeanName) {
			this.factoryBeanName = factoryBeanName;
			return this;
		}

		public Builder bean(Object bean) {
			this.bean = bean;
			return this;
		}

		public Builder method(Method method) {
			this.method = method;
			return this;
		}

		public Builder checkpointMode(@Nullable CheckpointMode checkpointMode) {
			this.checkpointMode = checkpointMode;
			return this;
		}

		public Builder retrievalMode(@Nullable RetrievalMode retrievalMode) {
			this.retrievalMode = retrievalMode;
			return this;
		}

		public Builder replyStream(@Nullable String replyStream) {
			this.replyStream = replyStream;
			return this;
		}

		public KclListenerEndpoint build() {
			return new KclListenerEndpoint(this);
		}

	}

}
