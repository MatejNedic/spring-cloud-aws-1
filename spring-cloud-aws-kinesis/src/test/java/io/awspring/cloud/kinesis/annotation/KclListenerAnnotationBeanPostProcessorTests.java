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

import static org.assertj.core.api.Assertions.assertThat;

import io.awspring.cloud.kinesis.config.KclBootstrapConfiguration;
import io.awspring.cloud.kinesis.config.KclListenerEndpoint;
import io.awspring.cloud.kinesis.config.MessageListenerContainerFactory;
import io.awspring.cloud.kinesis.listener.MessageListener;
import io.awspring.cloud.kinesis.listener.MessageListenerContainer;
import io.awspring.cloud.kinesis.listener.MessageListenerContainerRegistry;
import io.awspring.cloud.kinesis.listener.checkpoint.CheckpointMode;
import io.awspring.cloud.kinesis.listener.retrieval.RetrievalMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 * @author Matej Nedic
 * @since 4.2.0
 */
class KclListenerAnnotationBeanPostProcessorTests {

	@Test
	@DisplayName("bootstrap discovers @KclListener, resolves the endpoint and registers a container")
	void discoversAnnotatedMethodAndRegistersContainer() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
			CapturingContainerFactory factory = context.getBean(CapturingContainerFactory.class);
			assertThat(factory.endpoints).hasSize(1);
			KclListenerEndpoint endpoint = factory.endpoints.get(0);
			assertThat(endpoint.getId()).isEqualTo("order-listener");
			assertThat(endpoint.getStreamName()).isEqualTo("orders");
			assertThat(endpoint.getApplicationName()).isEqualTo("order-processor");
			assertThat(endpoint.getHandlerMethodFactory()).isNotNull();

			MessageListenerContainerRegistry registry = context.getBean(MessageListenerContainerRegistry.class);
			assertThat(registry.getContainerById("order-listener")).isNotNull();
			assertThat(registry.getListenerContainers()).hasSize(1);
		}
	}

	@Test
	@DisplayName("blank applicationName defaults to the container id")
	void applicationNameDefaultsToId() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				DefaultAppNameConfig.class)) {
			CapturingContainerFactory factory = context.getBean(CapturingContainerFactory.class);
			assertThat(factory.endpoints).hasSize(1);
			KclListenerEndpoint endpoint = factory.endpoints.get(0);
			assertThat(endpoint.getApplicationName()).isEqualTo(endpoint.getId());
		}
	}

	@Test
	@DisplayName("generates a default container id when none is provided")
	void generatesDefaultIdWhenNotProvided() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(NoIdConfig.class)) {
			KclListenerEndpoint endpoint = context.getBean(CapturingContainerFactory.class).endpoints.get(0);
			assertThat(endpoint.getId()).startsWith("io.awspring.cloud.kinesis.KclListenerEndpointContainer#");
			assertThat(endpoint.getApplicationName()).isEqualTo(endpoint.getId());
		}
	}

	@Test
	@DisplayName("resolves checkpoint and retrieval modes from the annotation")
	void resolvesCheckpointAndRetrievalMode() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ModesConfig.class)) {
			KclListenerEndpoint endpoint = context.getBean(CapturingContainerFactory.class).endpoints.get(0);
			assertThat(endpoint.getCheckpointMode()).isEqualTo(CheckpointMode.RECORD);
			assertThat(endpoint.getRetrievalMode()).isEqualTo(RetrievalMode.ENHANCED_FAN_OUT);
		}
	}

	@Test
	@DisplayName("leaves checkpoint and retrieval modes null when not specified")
	void leavesModesNullWhenNotSpecified() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
			KclListenerEndpoint endpoint = context.getBean(CapturingContainerFactory.class).endpoints.get(0);
			assertThat(endpoint.getCheckpointMode()).isNull();
			assertThat(endpoint.getRetrievalMode()).isNull();
			assertThat(endpoint.getReplyStream()).isNull();
		}
	}

	@Test
	@DisplayName("selects the container factory named by the annotation")
	void usesNamedFactoryWhenSpecified() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				NamedFactoryConfig.class)) {
			CapturingContainerFactory primary = context.getBean("primaryFactory", CapturingContainerFactory.class);
			CapturingContainerFactory secondary = context.getBean("secondaryFactory", CapturingContainerFactory.class);
			assertThat(primary.endpoints).isEmpty();
			assertThat(secondary.endpoints).hasSize(1);
			assertThat(secondary.endpoints.get(0).getFactoryBeanName()).isEqualTo("secondaryFactory");
		}
	}

	@Test
	@DisplayName("resolves the @SendTo destination into the endpoint reply stream")
	void resolvesSendToReplyStream() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SendToConfig.class)) {
			KclListenerEndpoint endpoint = context.getBean(CapturingContainerFactory.class).endpoints.get(0);
			assertThat(endpoint.getReplyStream()).isEqualTo("out-stream");
		}
	}

	@Test
	@DisplayName("resolves property placeholders in annotation attributes")
	void resolvesPlaceholders() {
		try (AnnotationConfigApplicationContext context = contextWith(PlaceholderConfig.class, "app.stream",
				"resolved-stream")) {
			KclListenerEndpoint endpoint = context.getBean(CapturingContainerFactory.class).endpoints.get(0);
			assertThat(endpoint.getStreamName()).isEqualTo("resolved-stream");
		}
	}

	@Test
	@DisplayName("resolves streamName provided via the value() alias")
	void resolvesStreamNameViaValueAlias() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				ValueAliasConfig.class)) {
			KclListenerEndpoint endpoint = context.getBean(CapturingContainerFactory.class).endpoints.get(0);
			assertThat(endpoint.getStreamName()).isEqualTo("aliased-stream");
		}
	}

	@Test
	@DisplayName("registers a container per annotated method on the same bean")
	void registersMultipleListenersFromSameBean() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MultiConfig.class)) {
			CapturingContainerFactory factory = context.getBean(CapturingContainerFactory.class);
			assertThat(factory.endpoints).hasSize(2);
			assertThat(factory.endpoints).extracting(KclListenerEndpoint::getId).containsExactlyInAnyOrder("first",
					"second");
			MessageListenerContainerRegistry registry = context.getBean(MessageListenerContainerRegistry.class);
			assertThat(registry.getListenerContainers()).hasSize(2);
		}
	}

	@Test
	@DisplayName("beans without @KclListener register no containers")
	void nonAnnotatedBeanRegistersNothing() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				NoListenerConfig.class)) {
			assertThat(context.getBean(CapturingContainerFactory.class).endpoints).isEmpty();
			assertThat(context.getBean(MessageListenerContainerRegistry.class).getListenerContainers()).isEmpty();
		}
	}

	private static AnnotationConfigApplicationContext contextWith(Class<?> configClass, String key, String value) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(key, value)));
		context.register(configClass);
		context.refresh();
		return context;
	}

	@Configuration
	@Import(KclBootstrapConfiguration.class)
	static class TestConfig {

		@Bean
		CapturingContainerFactory containerFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		OrderListener orderListener() {
			return new OrderListener();
		}

	}

	@Configuration
	@Import(KclBootstrapConfiguration.class)
	static class DefaultAppNameConfig {

		@Bean
		CapturingContainerFactory containerFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		NoAppNameListener noAppNameListener() {
			return new NoAppNameListener();
		}

	}

	@Configuration
	@Import(KclBootstrapConfiguration.class)
	static class NoIdConfig {

		@Bean
		CapturingContainerFactory containerFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		NoIdListener noIdListener() {
			return new NoIdListener();
		}

	}

	@Configuration
	@Import(KclBootstrapConfiguration.class)
	static class ModesConfig {

		@Bean
		CapturingContainerFactory containerFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		ModesListener modesListener() {
			return new ModesListener();
		}

	}

	@Configuration
	@Import(KclBootstrapConfiguration.class)
	static class NamedFactoryConfig {

		@Bean
		CapturingContainerFactory primaryFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		CapturingContainerFactory secondaryFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		NamedFactoryListener namedFactoryListener() {
			return new NamedFactoryListener();
		}

	}

	@Configuration
	@Import(KclBootstrapConfiguration.class)
	static class SendToConfig {

		@Bean
		CapturingContainerFactory containerFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		SendToListener sendToListener() {
			return new SendToListener();
		}

	}

	@Configuration
	@Import(KclBootstrapConfiguration.class)
	static class PlaceholderConfig {

		@Bean
		CapturingContainerFactory containerFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		PlaceholderListener placeholderListener() {
			return new PlaceholderListener();
		}

	}

	@Configuration
	@Import(KclBootstrapConfiguration.class)
	static class ValueAliasConfig {

		@Bean
		CapturingContainerFactory containerFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		ValueAliasListener valueAliasListener() {
			return new ValueAliasListener();
		}

	}

	@Configuration
	@Import(KclBootstrapConfiguration.class)
	static class MultiConfig {

		@Bean
		CapturingContainerFactory containerFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		MultiListener multiListener() {
			return new MultiListener();
		}

	}

	@Configuration
	@Import(KclBootstrapConfiguration.class)
	static class NoListenerConfig {

		@Bean
		CapturingContainerFactory containerFactory() {
			return new CapturingContainerFactory();
		}

		@Bean
		PlainBean plainBean() {
			return new PlainBean();
		}

	}

	static class NoIdListener {

		@KclListener(streamName = "events")
		void handle(String payload) {
		}

	}

	static class ModesListener {

		@KclListener(id = "modes", streamName = "orders", checkpointMode = "RECORD", retrievalMode = "ENHANCED_FAN_OUT")
		void handle(String payload) {
		}

	}

	static class NamedFactoryListener {

		@KclListener(id = "named", streamName = "orders", factory = "secondaryFactory")
		void handle(String payload) {
		}

	}

	static class SendToListener {

		@KclListener(id = "send-to", streamName = "orders")
		@SendTo("out-stream")
		String handle(String payload) {
			return payload;
		}

	}

	static class PlaceholderListener {

		@KclListener(id = "placeholder", streamName = "${app.stream}")
		void handle(String payload) {
		}

	}

	static class ValueAliasListener {

		@KclListener(value = "aliased-stream", id = "value-alias")
		void handle(String payload) {
		}

	}

	static class MultiListener {

		@KclListener(id = "first", streamName = "first-stream")
		void handleFirst(String payload) {
		}

		@KclListener(id = "second", streamName = "second-stream")
		void handleSecond(String payload) {
		}

	}

	static class PlainBean {

		void notAListener(String payload) {
		}

	}

	static class OrderListener {

		@KclListener(id = "order-listener", streamName = "orders", applicationName = "order-processor")
		void handle(String payload) {
		}

	}

	static class NoAppNameListener {

		@KclListener(id = "no-app", streamName = "events")
		void handle(String payload) {
		}

	}

	static class CapturingContainerFactory implements MessageListenerContainerFactory {

		private final List<KclListenerEndpoint> endpoints = new ArrayList<>();

		@Override
		public MessageListenerContainer createContainer(KclListenerEndpoint endpoint) {
			this.endpoints.add(endpoint);
			return new StubContainer(endpoint.getId());
		}

	}

	static class StubContainer implements MessageListenerContainer {

		private String id;

		private boolean running;

		StubContainer(String id) {
			this.id = id;
		}

		@Override
		public String getId() {
			return this.id;
		}

		@Override
		public void setId(String id) {
			this.id = id;
		}

		@Override
		public void start() {
			this.running = true;
		}

		@Override
		public void stop() {
			this.running = false;
		}

		@Override
		public boolean isRunning() {
			return this.running;
		}

		@Nullable
		MessageListener getMessageListener() {
			return null;
		}

	}

}
