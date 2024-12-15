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
package io.awspring.cloud.autoconfigure.config.parameterstore;

import io.awspring.cloud.autoconfigure.config.BootstrapLoggingHelper;
import io.awspring.cloud.parameterstore.ParameterStorePropertySource;
import java.util.Collections;
import java.util.Map;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.Nullable;
import software.amazon.awssdk.services.ssm.SsmClient;

/**
 * {@link ConfigDataLoader} for AWS Parameter Store.
 *
 * @author Eddú Meléndez
 * @author Maciej Walkowiak
 * @author Matej Nedic
 * @since 2.3.0
 */
public class ParameterStoreConfigDataLoader implements ConfigDataLoader<ParameterStoreConfigDataResource> {

	public ParameterStoreConfigDataLoader(DeferredLogFactory logFactory) {
		BootstrapLoggingHelper.reconfigureLoggers(logFactory,
				"io.awspring.cloud.parameterstore.ParameterStorePropertySource",
				"io.awspring.cloud.autoconfigure.config.parameterstore.ParameterStorePropertySources");
	}

	private static final ConfigData.PropertySourceOptions PROFILE_SPECIFIC = ConfigData.PropertySourceOptions
			.always(ConfigData.Option.PROFILE_SPECIFIC);

	private static final ConfigData.PropertySourceOptions NON_PROFILE_SPECIFIC = ConfigData.PropertySourceOptions.ALWAYS_NONE;

	@Override
	@Nullable
	public ConfigData load(ConfigDataLoaderContext context, ParameterStoreConfigDataResource resource) {
		try {

			ConfigData.PropertySourceOptions options = (resource.getProfile() != null) ? PROFILE_SPECIFIC
					: NON_PROFILE_SPECIFIC;
			// resource is disabled if parameter store integration is disabled via
			// spring.cloud.aws.parameterstore.enabled=false
			if (resource.isEnabled()) {
				SsmClient ssm = context.getBootstrapContext().get(SsmClient.class);
				ParameterStorePropertySource propertySource = resource.getPropertySources()
						.createPropertySource(resource.getContext(), resource.isOptional(), ssm);
				if (propertySource != null) {
					return new ConfigData(Collections.singletonList(propertySource), options);
				}
				else {
					return null;
				}
			}
			else {
				// create dummy empty config data
				return new ConfigData(
						Collections.singletonList(new MapPropertySource("aws-parameterstore:" + context, Map.of())),
						options);
			}
		}
		catch (Exception e) {
			throw new ConfigDataResourceNotFoundException(resource, e);
		}
	}

}
