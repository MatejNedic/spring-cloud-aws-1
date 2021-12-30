package io.awspring.cloud.v3.dynamodb.repository.query;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@QueryAnnotation
public @interface Query {

	 String value() default  "";
}
