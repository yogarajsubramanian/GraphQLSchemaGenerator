package com.yogaraj.graphql;

import com.yogaraj.graphql.schemagenerator.GraphQlSchemaGenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GraphqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlApplication.class, args);
	}

	@Bean
	GraphQlSchemaGenerator mGraphQlSchema() throws ClassNotFoundException, SecurityException, NullPointerException {
		GraphQlSchemaGenerator mQlSchema = new GraphQlSchemaGenerator();
		mQlSchema.generateSchema("com.yogaraj.graphql.dto");
		return mQlSchema;
	}
}
