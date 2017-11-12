package com.pat.ged;

import com.pat.ged.service.FileDocumentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    FileDocumentService fileDocumentService() {
        return new FileDocumentService();
    }

}