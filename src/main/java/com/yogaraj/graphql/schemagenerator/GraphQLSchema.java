package com.yogaraj.graphql.schemagenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inidcates the class is involved in GraphQL schema
 * 
 * @author Yogaraj
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GraphQLSchema {

    public enum SchemaType {
        QUERY("query"), MUTATION("mutation"), OBJECT("type"), INPUT("input"), INTERFACE("interface"),
        IMPLEMENTATION("implementation"), ENUM("enum");

        String type;

        SchemaType(String type) {
            this.type = type;
        }
    };

    Class<?> baseSchemaClassRef() default Class.class;

    SchemaType schemaType() default SchemaType.OBJECT;

    String operationName();
}
