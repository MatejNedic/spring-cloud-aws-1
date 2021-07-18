/*
 * Copyright 2013-2021 the original author or authors.
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

package io.awspring.cloud.secretsmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * This Pojo class is being used to map a single secret path of spring.config.import.
 * Instance of this class is used to determine if application should fail on missing
 * parameter.
 *
 * @author Matej Nedic
 * @since 2.3.2
 */
public class SecretKeyValue {

	/**
	 * Prefix which is used to mark key optional.
	 */
	public static final String optionalPrefix = "optional:";

	/**
	 * Value of key of Spring.config.import, in other words path used to find parameter
	 * located in Secret Manager.
	 */
	private String value;

	/**
	 * Used to determine is value optional or not.
	 */
	private Boolean isOptional;

	public static List<SecretKeyValue> createSecretValue(String keys) {
		List<SecretKeyValue> secretKeyValues = new ArrayList<>();
		if (StringUtils.hasLength(keys)) {
			List<String> listOfFields = Arrays.asList(keys.split(";"));
			listOfFields.forEach(field -> {
				if (field.length() > 8 && field.toLowerCase().startsWith(optionalPrefix)) {
					secretKeyValues.add(new SecretKeyValue(field.substring(9), Boolean.TRUE));
				}
				else {
					secretKeyValues.add(new SecretKeyValue(field, Boolean.FALSE));
				}
			});
		}
		return secretKeyValues;
	}

	public SecretKeyValue(String value, Boolean isOptional) {
		this.value = value;
		this.isOptional = isOptional;
	}

	public Boolean isOptional() {
		return isOptional;
	}

	public String getValue() {
		return value;
	}

}
