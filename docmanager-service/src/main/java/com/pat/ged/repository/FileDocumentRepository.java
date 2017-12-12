package com.pat.ged.repository;

import com.pat.ged.domain.FileDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Created by patricou on 08/11/2017.
 */
public interface FileDocumentRepository   extends ReactiveMongoRepository<FileDocument, String> {

    // the regex expr allows to do a like
    @Query("{ 'wordLines.word': {$regex : ?0}}")
    Flux<FileDocument> findFileDocumentsByWordLinesWord(final String word);

    // the regex expr allows to do a like
    @Query("{ 'filename': {$regex : ?0}}")
    Flux<FileDocument> findFileDocumentsByFilename(final String filename);

    //void deleteFileDocumentsByIdInGrid(final String idInGrid);

    @Query("{idInGrid : ?0}")
    Mono<FileDocument> findFileDocumentsByIdInGrid(final String idInGrid);

}
