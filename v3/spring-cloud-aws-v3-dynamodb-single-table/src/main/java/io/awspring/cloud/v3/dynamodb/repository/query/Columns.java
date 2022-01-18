package io.awspring.cloud.v3.dynamodb.repository.query;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Columns implements Iterable<StringColumnName> {
	private final Set<StringColumnName> columnNames;

	public static Columns empty() {
		return new Columns(Collections.emptySet());
	}

	public Columns(Set<StringColumnName> columnNames) {
		this.columnNames = Collections.unmodifiableSet(columnNames);
	}

	@Override
	public Iterator<StringColumnName> iterator() {
		return null;
	}

	@Override
	public void forEach(Consumer<? super StringColumnName> action) {
		Iterable.super.forEach(action);
	}

	@Override
	public Spliterator<StringColumnName> spliterator() {
		return Iterable.super.spliterator();
	}
}
