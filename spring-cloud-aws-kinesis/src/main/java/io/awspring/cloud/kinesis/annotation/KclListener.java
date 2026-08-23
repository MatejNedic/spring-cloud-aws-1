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
package io.awspring.cloud.kinesis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.messaging.handler.annotation.MessageMapping;

/**
 * Annotation that marks a method to be the target of a Kinesis message listener backed by the Kinesis Client Library
 * (KCL). Each annotated method is handled by a dedicated
 * {@link io.awspring.cloud.kinesis.listener.MessageListenerContainer}, created by the specified {@link #factory()}. If
 * not specified, a default factory is looked up in the context.
 * <p>
 * Properties in this annotation support property placeholders ({@code "${...}"}) and SpEL ({@code "#{...}"}).
 *
 * @author Matej Nedic
 * @since 4.2.0
 */
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MessageMapping
public @interface KclListener {

	/**
	 * The name of the Kinesis stream to consume from. Alias for {@link #streamName()}.
	 * @return the stream name.
	 */
	@AliasFor("streamName")
	String value() default "";

	/**
	 * An id for the {@link io.awspring.cloud.kinesis.listener.MessageListenerContainer} that will be created to handle
	 * this endpoint. If none is provided a default id will be generated.
	 * @return the container id.
	 */
	String id() default "";

	/**
	 * The name of the Kinesis stream to consume from. Alias for {@link #value()}.
	 * @return the stream name.
	 */
	@AliasFor("value")
	String streamName() default "";

	/**
	 * The KCL application name used for lease coordination. Defaults to the container id when not set.
	 * @return the application name.
	 */
	String applicationName() default "";

	/**
	 * The {@link io.awspring.cloud.kinesis.config.MessageListenerContainerFactory} bean name to be used to process this
	 * endpoint.
	 * @return the factory bean name.
	 */
	String factory() default "";

	/**
	 * The {@link io.awspring.cloud.kinesis.listener.checkpoint.CheckpointMode} to be used for this endpoint. If not
	 * specified, the mode defined on the container factory is used.
	 * @return the checkpoint mode.
	 */
	String checkpointMode() default "";

	/**
	 * The {@link io.awspring.cloud.kinesis.listener.retrieval.RetrievalMode} to be used for this endpoint. If not
	 * specified, the mode defined on the container factory is used.
	 * @return the retrieval mode.
	 */
	String retrievalMode() default "";

}
