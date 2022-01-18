package io.awspring.cloud.v3.dynamodb.core.query;

import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Iterator;

@FunctionalInterface
public interface Filter extends Streamable<CriteriaDefinition> {

	Iterable<CriteriaDefinition> getCriteriaDefinitions();

	static Filter from(CriteriaDefinition... criteriaDefinitions) {

		Assert.notNull(criteriaDefinitions, "CriteriaDefinitions must not be null");

		return from(Arrays.asList(criteriaDefinitions));
	}

	/**
	 * Create a simple {@link Filter} given {@link CriteriaDefinition}s.
	 *
	 * @param criteriaDefinitions must not be {@literal null}.
	 * @return the {@link Filter} object for {@link CriteriaDefinition}s.
	 */
	static Filter from(Iterable<? extends CriteriaDefinition> criteriaDefinitions) {

		Assert.notNull(criteriaDefinitions, "CriteriaDefinitions must not be null");

		return new DefaultFilter(criteriaDefinitions);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	default Iterator<CriteriaDefinition> iterator() {
		return getCriteriaDefinitions().iterator();
	}
}
