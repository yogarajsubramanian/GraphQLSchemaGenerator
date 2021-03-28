package com.yogaraj.graphql.schemagenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.yogaraj.graphql.schemagenerator.GraphQLField.FieldType;

/**
 * Indicates graphQL field parameters types
 * 
 * @author Yogaraj
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLFieldParameter {

    FieldType paramType() default FieldType.STRING;

    Class<?> paramObjectClass() default Class.class;

    String paramName();

    boolean isNullable() default true;
}
