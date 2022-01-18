package io.awspring.cloud.v3.dynamodb.repository.query;

import java.util.Optional;

public class StringColumnName {
	private final String columnName;

	StringColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Optional<String> getColumName() {
		return Optional.of(this.columnName);
	}

	public String toString() {
		return this.columnName;
	}
}
