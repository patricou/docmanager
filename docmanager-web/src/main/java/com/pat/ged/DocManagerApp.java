package com.pat.ged;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Hello DocManager
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
}
