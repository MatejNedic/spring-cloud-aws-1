/*
 * Copyright 2013-2023 the original author or authors.
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
package io.awspring.cloud.autoconfigure.config.s3;

import io.awspring.cloud.autoconfigure.config.AbstractAwsConfigDataLocationResolver;
import io.awspring.cloud.autoconfigure.core.*;
import io.awspring.cloud.autoconfigure.s3.AwsS3ClientCustomizer;
import io.awspring.cloud.autoconfigure.s3.S3KeysMissingException;
import io.awspring.cloud.autoconfigure.s3.properties.S3Properties;
import io.awspring.cloud.s3.crossregion.CrossRegionS3Client;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.util.ClassUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/**
 * Resolves config data locations in AWS S3.
 *
 * @author Kunal Varpe
 * @author Matej Nedic
 * @since 3.1.0
 */
public class S3ConfigDataLocationResolver extends AbstractAwsConfigDataLocationResolver<S3ConfigDataResource> {

	/**
	 * AWS S3 Config Data prefix.
	 */
	public static final String PREFIX = "aws-s3:";

	private final Log log;

	public S3ConfigDataLocationResolver(DeferredLogFactory deferredLogFactory) {
		this.log = deferredLogFactory.getLog(S3ConfigDataLocationResolver.class);
	}

	@Override
	protected String getPrefix() {
		return PREFIX;
	}

	@Override
	public List<S3ConfigDataResource> resolveProfileSpecific(ConfigDataLocationResolverContext resolverContext,
			ConfigDataLocation location, Profiles profiles) throws ConfigDataLocationNotFoundException {

		S3Properties s3Properties = loadProperties(resolverContext.getBinder());

		registerBean(resolverContext, AwsProperties.class, loadAwsProperties(resolverContext.getBinder()));
		registerBean(resolverContext, S3Properties.class, s3Properties);
		registerBean(resolverContext, CredentialsProperties.class,
				loadCredentialsProperties(resolverContext.getBinder()));
		registerBean(resolverContext, RegionProperties.class, loadRegionProperties(resolverContext.getBinder()));

		registerAndPromoteBean(resolverContext, S3Client.class, BootstrapRegistry.InstanceSupplier.of(
				S3ClientFactory.s3Client(createS3ClientBuilder(s3Properties, resolverContext.getBootstrapContext()))));

		S3PropertySources propertySources = new S3PropertySources();

		List<String> contexts = getCustomContexts(location.getNonPrefixedValue(PREFIX));

		List<S3ConfigDataResource> locations = new ArrayList<>();
		contexts.forEach(propertySourceContext -> locations.add(new S3ConfigDataResource(propertySourceContext,
				location.isOptional(), s3Properties.isEnableImport(), propertySources)));

		if (!location.isOptional() && locations.isEmpty()) {
			throw new S3KeysMissingException("No S3 keys provided in `spring.config.import=aws-s3:` configuration.");
		}

		return locations;
	}

	private S3ClientBuilder createS3ClientBuilder(S3Properties s3Properties, BootstrapContext context) {
		S3ClientBuilder builder = S3Client.builder();
		Optional.ofNullable(s3Properties.getCrossRegionEnabled()).ifPresent(builder::crossRegionAccessEnabled);
		builder.serviceConfiguration(s3Properties.toS3Configuration());

		builder = configure(builder, s3Properties, context);

		try {
			AwsS3ClientCustomizer configurer = context.get(AwsS3ClientCustomizer.class);
			if (configurer != null) {
				AwsClientCustomizer.apply(configurer, builder);
			}
		}
		catch (IllegalStateException e) {
			log.debug("Bean of type AwsClientConfigurerParameterStore is not registered: " + e.getMessage());
		}
		return builder;
	}

	protected S3Properties loadProperties(Binder binder) {
		return binder.bind(S3Properties.PREFIX, Bindable.of(S3Properties.class)).orElseGet(S3Properties::new);
	}

	static class S3ClientFactory {

		static S3Client s3Client(S3ClientBuilder builder) {
			if (ClassUtils.isPresent("io.awspring.cloud.s3.crossregion.CrossRegionS3Client", null)) {
				return new CrossRegionS3Client(builder);
			}
			else {
				return builder.build();
			}
		}
	}

}
