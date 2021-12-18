package io.awspring.cloud.v3.dynamodb.core.mapping;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ConditionExpression {

	/***
	 * @return an expression to be evaluated with Conditional Expressions in Put and Delete requests.
	 */
	String value() default "";
}
