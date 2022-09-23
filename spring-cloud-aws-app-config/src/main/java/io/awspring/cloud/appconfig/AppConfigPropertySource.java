/*
 * Copyright 2013-2022 the original author or authors.
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
package io.awspring.cloud.appconfig;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.lang.Nullable;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest;
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest;
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionResponse;

/**
 * @author Matej Nedic
 * @since 3.0
 */
public class AppConfigPropertySource extends EnumerablePropertySource<AppConfigDataClient> {

	// logger must stay static non-final so that it can be set with a value in
	// AppConfigDataLoader
	private static final Log LOG = LogFactory.getLog(AppConfigPropertySource.class);

	private static final String YAML_TYPE = "application/x-yaml";
	private static final String TEXT_TYPE = "text/plain";
	private static final String JSON_TYPE = "application/json";

	private final String configurationProfileIdentifier;
	private final String environmentIdentifier;
	private final String applicationIdentifier;
	private String sessionToken;
	private Properties properties;
	private GetLatestConfigurationResponse dataChanged;

	public AppConfigPropertySource(RequestContext requestContext, AppConfigDataClient appConfigDataClient) {
		super(requestContext.getContext(), appConfigDataClient);
		this.configurationProfileIdentifier = requestContext.getConfigurationProfileIdentifier();
		this.environmentIdentifier = requestContext.getEnvironmentIdentifier();
		this.applicationIdentifier = requestContext.getApplicationIdentifier();

	}

	public void init() throws IOException {
		if (dataChanged != null) {
			getParameters(dataChanged);
			this.dataChanged = null;
		}
		else {
			if (sessionToken == null) {
				StartConfigurationSessionRequest sessionRequest = StartConfigurationSessionRequest.builder()
					.environmentIdentifier(this.environmentIdentifier).applicationIdentifier(this.applicationIdentifier)
					.configurationProfileIdentifier(this.configurationProfileIdentifier).build();
				StartConfigurationSessionResponse response = this.source.startConfigurationSession(sessionRequest);
				sessionToken = response.initialConfigurationToken();
			}
			GetLatestConfigurationRequest request = GetLatestConfigurationRequest.builder().configurationToken(sessionToken)
				.build();
			GetLatestConfigurationResponse response = this.source.getLatestConfiguration(request);
			getParameters(response);
		}
	}

	@Override
	public String[] getPropertyNames() {
		Set<String> strings = this.properties.stringPropertyNames();
		return strings.toArray(new String[strings.size()]);
	}

	@Override
	@Nullable
	public Object getProperty(String name) {
		return this.properties.get(name);
	}

	private void getParameters(GetLatestConfigurationResponse  response) throws IOException {
		if (response.contentType().equals(YAML_TYPE) || response.contentType().equals(JSON_TYPE)) {
			resolveYamlOrJson(response);
		}
		else if (response.contentType().equals(TEXT_TYPE)) {
			resolveText(response);
		}
		sessionToken = response.nextPollConfigurationToken();
	}

	// YAML is a superset of JSON, which means you can parse JSON with a YAML parser
	private void resolveYamlOrJson(GetLatestConfigurationResponse response) {
		YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
		bean.setResources(new InputStreamResource(response.configuration().asInputStream()));
		properties = bean.getObject();
	}

	private void resolveText(GetLatestConfigurationResponse response) throws IOException {
		properties = new Properties();
		properties.load(response.configuration().asInputStream());
	}

	public Boolean areThereChanges() {
		GetLatestConfigurationRequest request = GetLatestConfigurationRequest.builder().configurationToken(sessionToken)
				.build();
		GetLatestConfigurationResponse response = source.getLatestConfiguration(request);
		if (response.contentType().isEmpty()) {
			sessionToken = response.nextPollConfigurationToken();
			this.dataChanged = null;
			return Boolean.FALSE;
		}
		else {
			sessionToken = response.nextPollConfigurationToken();
			this.dataChanged = response;
			return Boolean.TRUE;
		}
	}

}
