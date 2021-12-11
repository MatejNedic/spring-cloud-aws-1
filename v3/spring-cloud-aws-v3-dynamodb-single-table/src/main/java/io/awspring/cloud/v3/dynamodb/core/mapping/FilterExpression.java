package io.awspring.cloud.v3.dynamodb.core.mapping;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface FilterExpression {
	/***
	 * @return an expression to be evaluated with Query searches.
	 */
	String value() default "";
}
