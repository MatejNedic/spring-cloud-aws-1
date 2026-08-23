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
package io.awspring.cloud.kinesis.operations;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Matej Nedic
 * @since 4.2.0
 */
public interface KinesisOperations {

	SendResult send(String streamName, Object payload);

	SendResult send(String streamName, String partitionKey, Object payload);

	SendResult send(SendRequest request);

	CompletableFuture<SendResult> sendAsync(String streamName, String partitionKey, Object payload);

	CompletableFuture<SendResult> sendAsync(SendRequest request);

	BatchSendResult sendBatch(String streamName, List<SendRequest> requests);

	CompletableFuture<BatchSendResult> sendBatchAsync(String streamName, List<SendRequest> requests);

}
