package io.awspring.cloud.v3.dynamodb.core.query;

import io.awspring.cloud.v3.dynamodb.repository.query.StringColumnName;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.Optional;
import java.util.function.Function;

public interface CriteriaDefinition {

	StringColumnName getColumnName();

	Predicate getPredicate();

	class Predicate {
		private final Operators operator;
		private final @Nullable Object value;

		public Predicate(Operators operators, @Nullable Object value) {

			Assert.notNull(operators, "Operator must not be null");

			this.operator = operators;
			this.value = value;
		}

		public Operators getOperator() {
			return this.operator;
		}

		public Object getValue() {
			return this.value;
		}

		public <R> R as(Function<Object, ? extends R> mappingFunction) {
			return mappingFunction.apply(this.value);
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}

			if (!(o instanceof Predicate)) {
				return false;
			}

			Predicate predicate = (Predicate) o;

			if (!ObjectUtils.nullSafeEquals(operator, predicate.operator)) {
				return false;
			}

			return ObjectUtils.nullSafeEquals(value, predicate.value);
		}

		@Override
		public int hashCode() {
			int result = ObjectUtils.nullSafeHashCode(operator);
			result = 31 * result + ObjectUtils.nullSafeHashCode(value);
			return result;
		}

	}

	enum Operators {

		ONTAINS("CONTAINS"), CONTAINS_KEY("CONTAINS KEY"), EQ("="),
		NE("!="),
		IS_NOT_NULL("IS NOT NULL"),
		GT(">"), GTE(">="), LT("<"), LTE("<="), IN("IN"), LIKE("LIKE");

		public static Optional<Operators> from(String operator) {

			for (Operators operatorsValue : Operators.values()) {
				if (operatorsValue.toString().equals(operator)) {
					return Optional.of(operatorsValue);
				}
			}

			return Optional.empty();
		}

		private final String operator;

		Operators(String operator) {
			this.operator = operator;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.operator;
		}
	}

}
