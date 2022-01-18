package io.awspring.cloud.v3.dynamodb.repository.query;

import io.awspring.cloud.v3.dynamodb.core.coverter.DynamoDbConverter;
import io.awspring.cloud.v3.dynamodb.core.mapping.DynamoDbPersistentProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;

import java.util.Iterator;
import java.util.Optional;

class ConvertingParameterAccessor implements DynamoDbParameterAccessor {

	private final DynamoDbConverter converter;

	private final DynamoDbParameterAccessor delegate;

	ConvertingParameterAccessor(DynamoDbConverter converter, DynamoDbParameterAccessor delegate) {

		this.converter = converter;
		this.delegate = delegate;
	}

	@Override
	public Pageable getPageable() {
		return this.delegate.getPageable();
	}

	@Override
	public Sort getSort() {
		return this.delegate.getSort();
	}

	@Override
	public Optional<Class<?>> getDynamicProjection() {
		return this.delegate.getDynamicProjection();
	}

	@Override
	public Class<?> findDynamicProjection() {
		return null;
	}

	@Override
	public Object getBindableValue(int index) {
		return getConvertedValue(delegate.getBindableValue(index), null);
	}

	@Nullable
	private Object getConvertedValue(Object value, @Nullable TypeInformation<?> typeInformation) {
		return converter.convertToDynamoDbType(value, typeInformation == null ? null : typeInformation.getActualType());
	}

	@Override
	public boolean hasBindableNullValue() {
		return this.delegate.hasBindableNullValue();
	}

	@Override
	public Iterator<Object> iterator() {
		return new ConvertingIterator(delegate.iterator());
	}


	@Override
	public Object[] getValues() {
		return this.delegate.getValues();
	}

	@Override
	public QueryOptions getQueryOptions() {
		return this.delegate.getQueryOptions();
	}

	@Override
	public Class<?> getParameterType(int index) {
		return delegate.getParameterType(index);
	}

	private class ConvertingIterator implements PotentiallyConvertingIterator {

		private final Iterator<Object> delegate;

		private int index = 0;

		/**
		 * Create a new {@link ConvertingIterator} for the given delegate.
		 *
		 * @param delegate must not be {@literal null}.
		 */
		ConvertingIterator(Iterator<Object> delegate) {
			this.delegate = delegate;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return this.delegate.hasNext();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Nullable
		public Object next() {
			return delegate.next();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			this.delegate.remove();
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.cassandra.repository.query.ConvertingParameterAccessor.PotentiallyConvertingIterator#nextConverted(org.springframework.data.cassandra.core.mapping.CassandraPersistentProperty)
		 */
		@Nullable
		@Override
		public Object nextConverted(DynamoDbPersistentProperty property) {
			Object next = next();

			if (next == null) {
				return null;
			}
			return getConvertedValue(next, property.getTypeInformation());
		}


	}
	interface PotentiallyConvertingIterator extends Iterator<Object> {


		@Nullable
		Object nextConverted(DynamoDbPersistentProperty property);

	}
}
