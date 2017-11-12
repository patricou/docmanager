package com.pat.ged;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.stream.Stream;

/**
 * Unit test for simple toto.
 */
@DisplayName("DocManager Testing")
public class DocManagerAppTest {

    public static final Logger logger = LoggerFactory.getLogger( DocManagerAppTest.class );

    @BeforeAll
    static void setup() {
        logger.info("@BeforeAll - executes once before all test methods in this class");
    }

    @BeforeEach
    void init() {
        logger.info("@BeforeEach - executes before each test method in this class");
    }

    @Test
    @DisplayName("First test with Junit 5")
    void lambdaExpressions() {
        Assertions.assertTrue(Stream.of(1, 2, 3)
                .mapToInt(i -> i)
                .sum() > 4,"Sum should be greater than 5");
    }

    @Test
    @DisplayName("Test FileDocumentController test()")
    void testFileDocumentController(){

        WebTestClient client = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8080")
                .build();

        client.get().uri("/api/test").exchange().expectBody(String.class).isEqualTo("12345678910");

    }

    @AfterEach
    void tearDown() {
        logger.info("@AfterEach - executed after each test method.");
    }

    @AfterAll
    static void done() {
        logger.info("@AfterAll - executed after all test methods.");
    }

}
