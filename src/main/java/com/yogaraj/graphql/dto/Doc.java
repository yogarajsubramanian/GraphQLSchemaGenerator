package com.yogaraj.graphql.dto;

import com.yogaraj.graphql.schemagenerator.GraphQLField;
import com.yogaraj.graphql.schemagenerator.GraphQLFieldParameter;
import com.yogaraj.graphql.schemagenerator.GraphQLSchema;
import com.yogaraj.graphql.schemagenerator.GraphQLSchema.SchemaType;

@GraphQLSchema(schemaType = SchemaType.OBJECT, operationName = "doc")
public class Doc {

    @GraphQLField
    public String title;

    @GraphQLField(isNullable = false, parameters = { @GraphQLFieldParameter(paramName = "welcome"),
            @GraphQLFieldParameter(paramName = "test") })
    public String url;

}
