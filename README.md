**GraphQLSchemaGenerator** is a light-weight java component which converts Java DTO classes into graphQL schema. GraphQLSchemaGenerator  uses ClassPathScanningCandidateComponentProvider to find the components which are involved in schema generation process.

The primary need for creating **GraphQLSchemaGenerator** is there is no proper library/framework for java based server applications to map java dto classes and graphQL schema.

**Sample**

    GraphQlSchemaGenerator graphQlSchemaGenerator = new GraphQlSchemaGenerator();
    graphQlSchemaGenerator.generatedSchema("com.yogaraj.graphql.dto");
    String schema = graphQlSchemaGenerator.getSchema();

GraphQLSchemaGenerator uses specific annotations to identify and build graphQL schemas,
Below are the annotation which are employed in schema generation process.

**@GraphQLSchema** :  Indicates the particular class is involved in GraphQL schema creation process. 

    @GraphQLSchema(schemaType = SchemaType.OBJECT, operationName = "doc")
    public  class  Doc {}

**@GraphQLField** : Indicates the particular class variable is considered as GraphQL schema feild.

    @GraphQLSchema(schemaType = SchemaType.OBJECT, operationName = "doc")
    public  class  Doc {
    
    @GraphQLField
    public  String  title;
    
    @GraphQLField(isNullable = false)
    public  String  url;
    }

 **Supported GraphQL Features**

1. Fields 

2. parametes, multi-param 

3. Aliases 

4. operation name 

5. query, mutation,input 

6. not null 

7. enum

8. interface and implementation

9. unions 

10. extend 

**Features under work**

1. scalars

2. directives