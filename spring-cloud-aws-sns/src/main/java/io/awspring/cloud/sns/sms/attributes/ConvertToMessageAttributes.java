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
package io.awspring.cloud.sns.sms.attributes;

import static io.awspring.cloud.sns.core.MessageAttributeDataTypes.NUMBER;
import static io.awspring.cloud.sns.core.MessageAttributeDataTypes.STRING;

import java.util.Map;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;

interface ConvertToMessageAttributes {

	void convertAndPopulate(Map<String, MessageAttributeValue> attributeValueMap);

	static void populateMapWithStringValue(String attributeCode, String value,
			Map<String, MessageAttributeValue> messageAttributeValueMap) {
		if (value != null) {
			messageAttributeValueMap.put(attributeCode,
					MessageAttributeValue.builder().dataType(STRING).stringValue(value).build());
		}
	}

	static void populateMapWithNumberValue(String attributeCode, Number value,
			Map<String, MessageAttributeValue> messageAttributeValueMap) {
		if (value != null) {
			messageAttributeValueMap.put(attributeCode,
					MessageAttributeValue.builder().dataType(NUMBER).stringValue(value.toString()).build());
		}
	}

	static void populateMapWithNumberValue(String attributeCode, String value,
			Map<String, MessageAttributeValue> messageAttributeValueMap) {
		if (value != null) {
			messageAttributeValueMap.put(attributeCode,
					MessageAttributeValue.builder().dataType(NUMBER).stringValue(value).build());
		}
	}
}
