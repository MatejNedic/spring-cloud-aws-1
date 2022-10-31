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
package io.awspring.cloud.autoconfigure.config.appconfig;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * An {@link AbstractFailureAnalyzer} that performs analysis of a AppConfig configuration failure caused by not
 * providing a AppConfig key to `spring.config.import` property.
 *
 * @author Maciej Walkowiak
 * @author Matej Nedic
 * @since 3.0
 */
public class AppConfigMissingKeyFailureAnalyzer extends AbstractFailureAnalyzer<AppConfigKeysMissingException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, AppConfigKeysMissingException cause) {
		return new FailureAnalysis("Could not import properties from AWS AppConfig: " + cause.getMessage(),
				"Consider providing keys, for example `spring.config.import=aws-appconfig:myApllication___configProfile___environment` or disable integration with `spring.cloud.aws.appconfig.enabled=false",
				cause);
	}
}