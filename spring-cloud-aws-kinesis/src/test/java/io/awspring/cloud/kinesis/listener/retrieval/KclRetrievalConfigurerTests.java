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
package io.awspring.cloud.kinesis.listener.retrieval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.awspring.cloud.kinesis.listener.KclContainerOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.retrieval.RetrievalSpecificConfig;
import software.amazon.kinesis.retrieval.fanout.FanOutConfig;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

/**
 * @author Matej Nedic
 * @since 4.2.0
 */
class KclRetrievalConfigurerTests {

	private final KinesisAsyncClient kinesisClient = mock(KinesisAsyncClient.class);

	@Test
	@DisplayName("polling configurer builds a PollingConfig from the options")
	void pollingConfigurerBuildsPollingConfig() {
		KclContainerOptions options = KclContainerOptions.builder().maxRecords(500).build();

		RetrievalSpecificConfig config = new PollingRetrievalConfigurer().createRetrievalConfig("stream", "app",
				this.kinesisClient, options);

		assertThat(config).isInstanceOf(PollingConfig.class);
		PollingConfig pollingConfig = (PollingConfig) config;
		assertThat(pollingConfig.streamName()).isEqualTo("stream");
		assertThat(pollingConfig.maxRecords()).isEqualTo(500);
	}

	@Test
	@DisplayName("fan-out configurer builds a FanOutConfig with stream and application name")
	void fanOutConfigurerBuildsFanOutConfig() {
		RetrievalSpecificConfig config = new FanOutRetrievalConfigurer().createRetrievalConfig("stream", "app",
				this.kinesisClient, KclContainerOptions.builder().build());

		assertThat(config).isInstanceOf(FanOutConfig.class);
		FanOutConfig fanOutConfig = (FanOutConfig) config;
		assertThat(fanOutConfig.streamName()).isEqualTo("stream");
		assertThat(fanOutConfig.applicationName()).isEqualTo("app");
	}

}
