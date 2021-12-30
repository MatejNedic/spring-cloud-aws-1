package io.awspring.cloud.v3.dynamodb.repository.query;

import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.util.QueryExecutionConverters;
import org.springframework.data.repository.util.ReactiveWrapperConverters;
import org.springframework.data.repository.util.ReactiveWrappers;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class DynamoDbParameters extends Parameters<DynamoDbParameters, DynamoDbParameters.DynamoDbParameter> {

	private final @Nullable Integer queryOptionsIndex;

	public DynamoDbParameters(Method method) {

		super(method);

		this.queryOptionsIndex = Arrays.asList(method.getParameterTypes()).indexOf(QueryOptions.class);
	}

	private DynamoDbParameters(List<DynamoDbParameter> originals, @Nullable Integer queryOptionsIndex) {

		super(originals);

		this.queryOptionsIndex = queryOptionsIndex;
	}
	@Override
	protected DynamoDbParameter createParameter(MethodParameter parameter) {
		return new DynamoDbParameter(parameter);
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.repository.query.Parameters#createFrom(java.util.List)
	 */
	@Override
	protected DynamoDbParameters createFrom(List<DynamoDbParameter> parameters) {
		return new DynamoDbParameters(parameters, queryOptionsIndex);
	}

	public int getQueryOptionsIndex() {
		return (queryOptionsIndex != null ? queryOptionsIndex : -1);
	}

	public static class DynamoDbParameter extends Parameter {
		private final Class<?> parameterType;

		DynamoDbParameter(MethodParameter parameter) {
			super(parameter);

			AnnotatedParameter annotatedParameter = new AnnotatedParameter(parameter);
			parameterType = potentiallyUnwrapParameterType(parameter);
		}

		/* (non-Javadoc)
		 * @see org.springframework.data.repository.query.Parameter#isSpecialParameter()
		 */
		@Override
		public boolean isSpecialParameter() {
			return super.isSpecialParameter() || QueryOptions.class.isAssignableFrom(getType());
		}
		
		/* (non-Javadoc)
		 * @see org.springframework.data.repository.query.Parameter#getType()
		 */
		@Override
		public Class<?> getType() {
			return this.parameterType;
		}

		/**
		 * Returns the component type if the given {@link MethodParameter} is a wrapper type and the wrapper should be
		 * unwrapped.
		 *
		 * @param parameter must not be {@literal null}.
		 */
		private static Class<?> potentiallyUnwrapParameterType(MethodParameter parameter) {

			Class<?> originalType = parameter.getParameterType();

			if (isWrapped(parameter) && shouldUnwrap(parameter)) {
				return ResolvableType.forMethodParameter(parameter).getGeneric(0).getRawClass();
			}

			return originalType;
		}

		/**
		 * Returns whether the {@link MethodParameter} is wrapped in a wrapper type.
		 *
		 * @param parameter must not be {@literal null}.
		 * @see QueryExecutionConverters
		 */
		private static boolean isWrapped(MethodParameter parameter) {
			return QueryExecutionConverters.supports(parameter.getParameterType())
				|| ReactiveWrapperConverters.supports(parameter.getParameterType());
		}

		/**
		 * Returns whether the {@link MethodParameter} should be unwrapped.
		 *
		 * @param parameter must not be {@literal null}.
		 * @see QueryExecutionConverters
		 */
		private static boolean shouldUnwrap(MethodParameter parameter) {
			return QueryExecutionConverters.supportsUnwrapping(parameter.getParameterType())
				|| ReactiveWrappers.supports(parameter.getParameterType());
		}
	}

	static class AnnotatedParameter implements AnnotatedElement {

		private final MethodParameter methodParameter;

		AnnotatedParameter(MethodParameter methodParameter) {
			this.methodParameter = methodParameter;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		@Nullable
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return methodParameter.getParameterAnnotation(annotationClass);
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public Annotation[] getAnnotations() {
			return methodParameter.getParameterAnnotations();
		}

		/**
		 * @inheritDoc
		 */
		@Override
		public Annotation[] getDeclaredAnnotations() {
			return methodParameter.getParameterAnnotations();
		}
	}
}
