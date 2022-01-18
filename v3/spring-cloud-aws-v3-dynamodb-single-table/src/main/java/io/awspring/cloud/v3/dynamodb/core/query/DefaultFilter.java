package io.awspring.cloud.v3.dynamodb.core.query;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultFilter implements Filter {

	private final Iterable<CriteriaDefinition> criteriaDefinitions;

	public DefaultFilter(Iterable<? extends CriteriaDefinition> criteriaDefinitions) {
		this.criteriaDefinitions = (Iterable) criteriaDefinitions;
	}

	@Override
	public Iterable<CriteriaDefinition> getCriteriaDefinitions() {
		return this.criteriaDefinitions;
	}

	@Override
	public Iterator<CriteriaDefinition> iterator() {
		return Filter.super.iterator();
	}

	@Override
	public String toString() {
		return StreamSupport.stream(this.spliterator(), false)
			.map(Object::toString)
			.collect(Collectors.joining(" AND "));
	}
}
