package com.yogaraj.graphql.dto;

import com.yogaraj.graphql.schemagenerator.GraphQLField;
import com.yogaraj.graphql.schemagenerator.GraphQLSchema;
import com.yogaraj.graphql.schemagenerator.GraphQLField.FieldType;
import com.yogaraj.graphql.schemagenerator.GraphQLSchema.SchemaType;

@GraphQLSchema(operationName = "docType", schemaType = SchemaType.ENUM)
public enum DocType {
    @GraphQLField(fieldName = "media", fieldType = FieldType.STRING)
    MEDIA, 
    @GraphQLField(fieldName = "document", fieldType = FieldType.STRING)
    DOCUMENT
}
