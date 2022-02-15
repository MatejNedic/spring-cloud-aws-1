package io.awspring.cloud.v3.dynamodb.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class DynamoDBPageRequest extends PageRequest {



	/**
	 * Creates a new {@link PageRequest} with sort parameters applied.
	 *
	 * @param page zero-based page index, must not be negative.
	 * @param size the size of the page to be returned, must be greater than 0.
	 * @param sort must not be {@literal null}, use {@link Sort#unsorted()} instead.
	 */
	protected DynamoDBPageRequest(int page, int size, Sort sort) {
		super(page, size, sort);
	}
}
