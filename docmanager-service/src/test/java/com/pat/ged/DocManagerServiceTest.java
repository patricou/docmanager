package com.pat.ged;


import com.pat.ged.domain.WordLines;
import com.pat.ged.service.FileDocumentService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import java.util.List;

/**
 * Unit test.
 */
@ExtendWith( FileDocumentServiceParameterResolver.class)
public class DocManagerServiceTest {

    private static final Logger logger = LoggerFactory.getLogger( DocManagerServiceTest.class );

    @Test
    @DisplayName("Test FileDocumentService getWordsLines()")
    void testFDC( FileDocumentService fileDocumentService)
    {
        List<WordLines> strings = fileDocumentService.getWordsLines(Flux.just("hello my friends","how are you today, hum ? "));
        strings.sort((w1,w2)-> w1.getWord().toString().compareTo(w2.getWord().toString()));
        strings.forEach(i -> logger.info(i.toString()));

        assertTrue(strings.size() == 7,"Should be  element ( 7 words )");

    }

    @Test
    @DisplayName("Different tests")
    void testDifferents(){

    }
}
