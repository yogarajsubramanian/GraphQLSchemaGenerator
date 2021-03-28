package com.yogaraj.graphql.schemagenerator;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import com.yogaraj.graphql.schemagenerator.GraphQLField.FieldType;
import com.yogaraj.graphql.schemagenerator.GraphQLSchema.SchemaType;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

/**
 * GraphQLSchemaGenerator ligh-weigth java component which converts Java DTO
 * classes into graphQL schema. This class uses
 * {@link ClassPathScanningCandidateComponentProvider} to find the components
 * which are involved in schema generation process.
 * 
 * <p>
 * The primary need for creating GraphQlSchemaGenerator is there is no proper
 * library/framework for java based server applications to map java dto classes
 * and graphQL schema.
 * </p>
 * 
 * Example: {@link GraphQlSchemaGenerator#generateSchema(String...)} accepts
 * {@link String} array which contains the packages that need to be scanned.
 * 
 * <blockquote>
 * 
 * <pre>
 * GraphQlSchemaGenerator graphQlSchemaGenerator = new GraphQlSchemaGenerator();
 * graphQlSchemaGenerator.generatedSchema("com.yogaraj.graphql.dto");
 * </pre>
 * 
 * </blockquote>
 * 
 * once schema is generated we can get the generated schema {@link String} using
 * {@link GraphQlSchemaGenerator#getSchema()}
 * 
 * @author Yogaraj
 * @see https://graphql.org/learn/schema
 */
@Component
public class GraphQlSchemaGenerator {

    private String generatedSchema = "";

    private Logger logger = Logger.getLogger(GraphQlSchemaGenerator.class.getSimpleName());

    /**
     * since multiple Query and Mutation will be created its necessary to use
     * GraphQL's "extend" functionality to avoid conflits.
     * 
     * @see https://docs.reactioncommerce.com/docs/how-to-extend-graphql-to-add-field
     */
    boolean isQuerySchemaBuild, isMutationSchemaBuild;

    private void appendSchema(String schema) {
        generatedSchema = generatedSchema.concat(schema);
    }

    /**
     * @throws ClassNotFoundException when accessing {@link Class} objects
     * @throws SecurityException      when accessing private fields
     * @throws NullPointerException   when accessing {@link Class} objects,
     */
    public void generateSchema(String... packages)
            throws ClassNotFoundException, SecurityException, NullPointerException {

        this.printLogMessage("START: GraphQL schema generation ");

        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                true);
        provider.addIncludeFilter(new AnnotationTypeFilter(GraphQLSchema.class));

        // get list of classes which are annotated by {@link GraphQLSchema}
        for (String sPackage : packages) {
            for (BeanDefinition definition : provider.findCandidateComponents(sPackage)) {

                Class<?> graphQLClass = Class.forName(definition.getBeanClassName());
                GraphQLSchema sGraphQLSchema = graphQLClass.getAnnotation(GraphQLSchema.class);
                if (sGraphQLSchema == null) {
                    this.printLogMessage("IGNORING: Class " + graphQLClass.getName() + " should be annotated");
                    continue;
                }

                StringBuilder schemaBuilder = new StringBuilder();

                this.addSpecialCharacters(schemaBuilder, "new-line");
                // schema type
                this.addSchemaType(sGraphQLSchema, schemaBuilder);

                // operation name
                this.addOperationName(sGraphQLSchema, schemaBuilder);
                // schema implementation
                this.addImplementation(sGraphQLSchema, schemaBuilder);

                this.addSpecialCharacters(schemaBuilder, "delimiter-start");

                this.addSchemaFields(schemaBuilder, graphQLClass, sGraphQLSchema);

                this.addSpecialCharacters(schemaBuilder, "delimiter-end");

                this.addSpecialCharacters(schemaBuilder, "new-line");

                this.appendSchema(schemaBuilder.toString());
            }
        }

        this.printLogMessage("END: GraphQL schema generation ");
        this.printLogMessage(this.generatedSchema);
    }

    /**
     * add schema action type which indicates whether its a query, mutation or etc.
     * 
     * if schema type is query or mutation we will be adding keyword "extend" to
     * avoid conflicts.
     * 
     * Example: <blockquote>
     * 
     * <pre>
     * type Book {}
     * {@link GraphQLSchema.SchemaType#OBJECT} 
     * 
     * type Query{}
     * {@link GraphQLSchema.SchemaType#QUERY}
     * 
     * type Mutation{}
     * {@link GraphQLSchema.SchemaType#MUTATION}
     * 
     * interface Author{}
     * {@link GraphQLSchema.SchemaType#INTERFACE}
     * 
     * enum BookType{}
     *  {@link GraphQLSchema.SchemaType#ENUM}
     * 
     * input BookInput{}
     *  {@link GraphQLSchema.SchemaType#INPUT}
     * 
     * &#64;GraphQLSchema(type=?)
     * class SampleDto{}
     * 
     * </pre>
     * 
     * </blockquote>
     * 
     * @param sGraphQLSchema
     * @param schemaBuilder
     * 
     * @see https://www.apollographql.com/docs/apollo-server/schema/schema/
     */
    private void addSchemaType(GraphQLSchema sGraphQLSchema, StringBuilder schemaBuilder) {
        GraphQLSchema.SchemaType schemaType = sGraphQLSchema.schemaType();
        // check if extend is needed.
        switch (schemaType) {
        case QUERY:
            if (isQuerySchemaBuild) {
                schemaBuilder.append("extend ");
            } else {
                isQuerySchemaBuild = true;
            }
            break;
        case MUTATION:
            if (isMutationSchemaBuild) {
                schemaBuilder.append("extend ");
            } else {
                isMutationSchemaBuild = true;
            }
            break;
        default:
            break;
        }
        this.addSpecialCharacters(schemaBuilder, "white-space");
        if (schemaType != SchemaType.IMPLEMENTATION)
            schemaBuilder.append(schemaType.type);
        this.addSpecialCharacters(schemaBuilder, "white-space");
    }

    /**
     * The operation name is a meaningful and explicit name for your operation. It
     * is only required in multi-operation documents, but its use is encouraged
     * because it is very helpful for debugging and server-side logging
     * 
     * type Book {} {@link GraphQLSchema#operationName()}
     * 
     * @param sGraphQLSchema
     * @param schemaBuilder
     * @see https://graphql.org/learn/queries/#operation-name
     */
    private void addOperationName(GraphQLSchema sGraphQLSchema, StringBuilder schemaBuilder) {
        SchemaType mType = sGraphQLSchema.schemaType();
        if (!(mType == SchemaType.QUERY || mType == SchemaType.MUTATION))
            schemaBuilder.append(sGraphQLSchema.operationName());
        this.addSpecialCharacters(schemaBuilder, "white-space");
    }

    /**
     * add implementation syntax
     * 
     * <pre>
     * type Book implements Author {}
     * {@link GraphQLSchema#baseSchemaClassRef()}
     * {@link GraphQLSchema.SchemaType#IMPLEMENTATION}
     * class Book{}
     * </pre>
     * 
     * @param sGraphQLSchema
     * @param schemaBuilder
     */
    private void addImplementation(GraphQLSchema sGraphQLSchema, StringBuilder schemaBuilder) {
        if (sGraphQLSchema.schemaType() != SchemaType.IMPLEMENTATION) {
            return;
        }
        if (sGraphQLSchema.baseSchemaClassRef() == Class.class) {
            this.printLogMessage("IGNORING: Class implementation since no class reference is provided");
            return;
        }
        // verify if the extending class is a graphQL schema
        Class<?> baseSchemaClass = sGraphQLSchema.baseSchemaClassRef();
        GraphQLSchema bGraphQLSchema = baseSchemaClass.getAnnotation(GraphQLSchema.class);
        if (bGraphQLSchema == null) {
            this.printLogMessage("IGNORING: Class implementation since invalid class reference is provided");
            return;
        }

        if (bGraphQLSchema.schemaType() != SchemaType.OBJECT) {
            this.printLogMessage(
                    "IGNORING: Class implementation since invalid class reference is provided (Need to be of type OBJECT)");
            return;
        }

        // implements schema
        schemaBuilder.append("implements ");
        schemaBuilder.append(bGraphQLSchema.operationName());
    }

    /**
     * <blockquote>
     * 
     * <pre>
     * 
     * type Character {
        id: ID!
        name: String!
        friends: [Character]
        appearsIn: [Episode]!
        }
     * </pre>
     * 
     * </blockquote>
     * 
     * @param schemaBuilder
     * @param graphQLClass
     * @param sGraphQLSchema
     * @throws SecurityException
     * @throws NullPointerException
     * @see https://graphql.org/learn/queries/#fields
     */
    private void addSchemaFields(StringBuilder schemaBuilder, Class<?> graphQLClass, GraphQLSchema sGraphQLSchema)
            throws SecurityException, NullPointerException {
        Field[] fields = graphQLClass.getFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(GraphQLField.class)) {
                this.printLogMessage("IGNORING: field " + graphQLClass.getName() + " should be annotated");
                continue;
            }
            GraphQLField mGraphQLField = field.getAnnotation(GraphQLField.class);
            if (mGraphQLField == null) {
                continue;
            }
            // key: type
            String key = mGraphQLField.fieldName();
            if (key == null || key.equals(""))
                key = field.getName();

            schemaBuilder.append(key);

            if (sGraphQLSchema.schemaType() != SchemaType.ENUM) {
                String type = mGraphQLField.fieldType().type;
                if (mGraphQLField.fieldType() == FieldType.OBJECT) {
                    Class<?> fieldObjectClass = field.getDeclaringClass();
                    GraphQLSchema sFieldObjectSchema = fieldObjectClass.getAnnotation(GraphQLSchema.class);
                    if (sFieldObjectSchema == null) {
                        this.printLogMessage("IGNORING: field " + fieldObjectClass.getName() + " should be annotated");
                        continue; // object should be annotated using graphql
                    }
                    type = sFieldObjectSchema.operationName();
                }
                this.addSchemaFieldParams(mGraphQLField, schemaBuilder);
                this.addSpecialCharacters(schemaBuilder, "key-indicator");
                if (mGraphQLField.fieldType() == FieldType.LIST) {
                    this.addSpecialCharacters(schemaBuilder, "list-start");
                    schemaBuilder.append(type);
                    this.addSpecialCharacters(schemaBuilder, "list-end");
                } else {
                    schemaBuilder.append(type);
                }
                if (!mGraphQLField.isNullable())
                    this.addSpecialCharacters(schemaBuilder, "not-null");
            }

            this.addSpecialCharacters(schemaBuilder, "new-line");
        }
    }

    // params (key: type, key: type!, key: [type]!)
    private void addSchemaFieldParams(GraphQLField mGraphQLField, StringBuilder schemaBuilder) {
        if (mGraphQLField.parameters().length == 0) {
            return;
        }

        StringBuilder paramBuilder = new StringBuilder();

        int index = 0;
        for (GraphQLFieldParameter gFieldParameter : mGraphQLField.parameters()) {
            if (index > 0)
                this.addSpecialCharacters(paramBuilder, "param-seperator");

            String key = gFieldParameter.paramName();
            String type = gFieldParameter.paramType().type;

            if (gFieldParameter.paramType() == FieldType.OBJECT) {

                Class<?> fieldParamObjectClass = gFieldParameter.paramObjectClass();
                if (fieldParamObjectClass == Class.class) {
                    this.printLogMessage(
                            "IGNORING: param " + key + " is of type object but no class reference is provided");
                    continue;
                }
                GraphQLSchema sFieldObjectSchema = fieldParamObjectClass.getAnnotation(GraphQLSchema.class);
                if (sFieldObjectSchema == null) {
                    this.printLogMessage("IGNORING: param " + fieldParamObjectClass.getName() + " should be annotated");
                    continue; // object should be annotated using graphql
                }
                type = sFieldObjectSchema.operationName();
            }

            paramBuilder.append(key);
            this.addSpecialCharacters(paramBuilder, "key-indicator");
            if (gFieldParameter.paramType() == FieldType.LIST) {
                this.addSpecialCharacters(paramBuilder, "list-start");
                paramBuilder.append(type);
                this.addSpecialCharacters(paramBuilder, "list-end");
            } else {
                paramBuilder.append(type);
            }

            if (!gFieldParameter.isNullable())
                this.addSpecialCharacters(paramBuilder, "not-null");

            index++;
        }

        // Handle if there is no valid params passed
        if (index > 0) {
            this.addSpecialCharacters(schemaBuilder, "param-start");
            schemaBuilder.append(paramBuilder);
            this.addSpecialCharacters(schemaBuilder, "param-end");
        }
    }

    /**
     * adds special characters which is used to build and pretify graphql schema
     * 
     * <pre>
     * "{" => delimiter-start
     * "}" => delimiter-end
     * "[" => list-start
     * "!" => not-null
     * "]" => list-end
     * ":" => key-indicator
     * "(" => param-start
     * ")" => param-end
     * </pre>
     * 
     * @param schemaBuilder {@link StringBuilder}
     * @param type          {@link String} indicates the type of special charaters
     *                      that need to be appended
     */
    private void addSpecialCharacters(StringBuilder schemaBuilder, String type) {
        switch (type) {
        case "white-space":
            schemaBuilder.append(" ");
            break;
        case "delimiter-start":
            schemaBuilder.append("{\n");
            break;
        case "delimiter-end":
            schemaBuilder.append("}\n");
            break;
        case "new-line":
            schemaBuilder.append("\n");
            break;
        case "not-null":
            schemaBuilder.append("!");
            break;
        case "list-start":
            schemaBuilder.append("[");
            break;
        case "list-end":
            schemaBuilder.append("]");
            break;
        case "key-indicator":
            schemaBuilder.append(": ");
            break;
        case "param-start":
            schemaBuilder.append("( ");
            break;
        case "param-end":
            schemaBuilder.append(") ");
            break;
        case "param-seperator":
            schemaBuilder.append(",");
            break;
        }
    }

    public String getSchema() {
        return this.generatedSchema;
    }

    private void printLogMessage(String message) {
        this.logger.info(message);
    }
}
