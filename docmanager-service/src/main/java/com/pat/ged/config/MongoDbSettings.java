package com.pat.ged.config;

import com.mongodb.MongoClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDbSettings {
    static class OptionsConfig {

        @Bean
        public MongoClientOptions mongoOptions() {
            return MongoClientOptions.builder()
                    .connectTimeout(2000)
                    .socketTimeout(2000)
                    .maxWaitTime(2000)
                    .build();
        }

    }
}