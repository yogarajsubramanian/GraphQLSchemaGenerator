package com.yogaraj.graphql.schemagenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates graphQL field with in the schema
 * 
 * @author Yogaraj
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLField {

    enum FieldType {
        STRING("String"), INT("Int"), FLOAT("Float"), BOOLEAN("Boolean"), ID("ID"), OBJECT("object"), LIST("list");

        String type;

        FieldType(String type) {
            this.type = type;
        }
    }

    FieldType fieldType() default FieldType.STRING;

    String fieldName() default "";

    boolean isNullable() default true;

    GraphQLFieldParameter[] parameters() default {};
}
