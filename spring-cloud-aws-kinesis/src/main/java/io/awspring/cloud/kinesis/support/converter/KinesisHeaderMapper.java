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
package io.awspring.cloud.kinesis.support.converter;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

/**
 * @author Matej Nedic
 * @since 4.2.0
 */
class KinesisHeaderMapper {

	public Map<String, Object> toHeaders(KinesisClientRecord record, @Nullable String shardId, String streamName) {
		Map<String, Object> headers = new HashMap<>();
		headers.put(KinesisHeaders.PARTITION_KEY, record.partitionKey());
		headers.put(KinesisHeaders.SEQUENCE_NUMBER, record.sequenceNumber());
		headers.put(KinesisHeaders.SUBSEQUENCE_NUMBER, record.subSequenceNumber());
		if (record.approximateArrivalTimestamp() != null) {
			headers.put(KinesisHeaders.APPROXIMATE_ARRIVAL_TIMESTAMP, record.approximateArrivalTimestamp());
		}
		if (shardId != null) {
			headers.put(KinesisHeaders.SHARD_ID, shardId);
		}
		headers.put(KinesisHeaders.STREAM_NAME, streamName);
		return headers;
	}

}
