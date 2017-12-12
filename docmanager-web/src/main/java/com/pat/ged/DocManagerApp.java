package com.pat.ged;

import io.swagger.annotations.Api;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *  DocManager by Patrick Deschamps
 *
 */

@EnableReactiveMongoRepositories
@AutoConfigureAfter(EmbeddedMongoAutoConfiguration.class)
@SpringBootApplication(scanBasePackages={"com.pat"})
@EnableSwagger2
public class DocManagerApp implements CommandLineRunner{

    public static void main(String[] args) {
        SpringApplication.run(DocManagerApp.class, args);
    }

    public void run(String... arg0) throws Exception{

    }

    // for Swagger
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors
                        .any()).build().pathMapping("/")
                .apiInfo(apiInfo()).useDefaultResponseMessages(false);
    }

    // for Swagger description
    @Bean
    public ApiInfo apiInfo() {

        final ApiInfoBuilder builder = new ApiInfoBuilder();
        builder.title("DocManager API through Swagger UI").version("1.0").license("(C) Copyright Patrick Deschamps")
                .description("List of all the APIs of DocManager App through Swagger UI.( Don't hesitate to copy the requets URL in the browser to see the reactive result).");
        return builder.build();
    }
}

