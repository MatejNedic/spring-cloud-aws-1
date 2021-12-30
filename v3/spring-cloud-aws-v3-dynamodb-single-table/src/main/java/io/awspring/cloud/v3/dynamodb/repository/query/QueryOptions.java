package io.awspring.cloud.v3.dynamodb.repository.query;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

public class QueryOptions {
	private static final QueryOptions EMPTY = QueryOptions.builder().build();


	private final @Nullable Integer pageSize;
	private final @Nullable Boolean tracing;

	protected QueryOptions( @Nullable Integer pageSize, @Nullable Boolean tracing) {
		this.pageSize = pageSize;
		this.tracing = tracing;
	}

	/**
	 * Create a new {@link QueryOptionsBuilder}.
	 *
	 * @return a new {@link QueryOptionsBuilder}.
	 * @since 1.5
	 */
	public static QueryOptionsBuilder builder() {
		return new QueryOptionsBuilder();
	}

	/**
	 * Create default {@link QueryOptions}.
	 *
	 * @return default {@link QueryOptions}.
	 * @since 2.0
	 */
	public static QueryOptions empty() {
		return EMPTY;
	}

	/**
	 * Create a new {@link QueryOptionsBuilder} to mutate properties of this {@link QueryOptions}.
	 *
	 * @return a new {@link QueryOptionsBuilder} initialized with this {@link QueryOptions}.
	 * @since 2.0
	 */
	public QueryOptionsBuilder mutate() {
		return new QueryOptionsBuilder(this);
	}

	/**
	 * @return the number of rows to fetch per chunking request. May be {@literal null} if not set.
	 * @since 1.5
	 */
	@Nullable
	protected Integer getPageSize() {
		return this.pageSize;
	}

	/**
	 * @return whether to enable tracing. May be {@literal null} if not set.
	 */
	@Nullable
	protected Boolean getTracing() {
		return this.tracing;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof QueryOptions)) {
			return false;
		}

		QueryOptions options = (QueryOptions) o;

		if (!ObjectUtils.nullSafeEquals(pageSize, options.pageSize)) {
			return false;
		}


		if (!ObjectUtils.nullSafeEquals(tracing, options.tracing)) {
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public int hashCode() {
		int result = ObjectUtils.nullSafeHashCode(pageSize);
		result = 31 * result + ObjectUtils.nullSafeHashCode(tracing);
		return result;
	}

	/**
	 * Builder for {@link QueryOptions}.
	 *
	 * @author Mark Paluch
	 * @since 1.5
	 */
	public static class QueryOptionsBuilder {


		protected @Nullable Integer pageSize;

		protected @Nullable Boolean tracing;

		QueryOptionsBuilder() {}

		QueryOptionsBuilder(QueryOptions queryOptions) {
			this.pageSize = queryOptions.pageSize;
			this.tracing = queryOptions.tracing;
		}
		public QueryOptionsBuilder pageSize(int pageSize) {

			Assert.isTrue(pageSize >= 0, "Page size must be greater than equal to zero");

			this.pageSize = pageSize;

			return this;
		}

		/**
		 * Enables statement tracing.
		 *
		 * @param tracing {@literal true} to enable statement tracing to the executed statements.
		 * @return {@code this} {@link QueryOptionsBuilder}
		 */
		public QueryOptionsBuilder tracing(boolean tracing) {

			this.tracing = tracing;

			return this;
		}

		/**
		 * Enables statement tracing.
		 *
		 * @return {@code this} {@link QueryOptionsBuilder}
		 */
		public QueryOptionsBuilder withTracing() {
			return tracing(true);
		}

		/**
		 * Builds a new {@link QueryOptions} with the configured values.
		 *
		 * @return a new {@link QueryOptions} with the configured values
		 */
		public QueryOptions build() {
			return new QueryOptions(this.pageSize,this.tracing);
		}
	}
}
