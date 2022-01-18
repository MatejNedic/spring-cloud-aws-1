package io.awspring.cloud.v3.dynamodb.core.query;

import io.awspring.cloud.v3.dynamodb.repository.query.Columns;
import io.awspring.cloud.v3.dynamodb.repository.query.QueryOptions;
import org.springframework.data.domain.Sort;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Query implements Filter {

	private static final Query EMPTY = new Query(Collections.emptyList(), Columns.empty(), Sort.unsorted(),
		Optional.empty(), Optional.empty(), Optional.empty(), false);

	private final boolean allowFiltering;

	private final Columns columns;

	private final List<CriteriaDefinition> criteriaDefinitions;

	private final Optional<Long> limit;

	private final Optional<ByteBuffer> pagingState;

	private final Optional<QueryOptions> queryOptions;

	private final Sort sort;

	private Query(List<CriteriaDefinition> criteriaDefinitions, Columns columns, Sort sort,
				  Optional<ByteBuffer> pagingState, Optional<QueryOptions> queryOptions, Optional<Long> limit,
				  boolean allowFiltering) {

		this.criteriaDefinitions = criteriaDefinitions;
		this.columns = columns;
		this.sort = sort;
		this.pagingState = pagingState;
		this.queryOptions = queryOptions;
		this.limit = limit;
		this.allowFiltering = allowFiltering;
	}

	@Override
	public Iterable<CriteriaDefinition> getCriteriaDefinitions() {
		return Collections.unmodifiableCollection(criteriaDefinitions);
	}
}
