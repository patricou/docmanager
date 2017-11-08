package com.pat.ged.repository;

import com.pat.ged.domain.FileDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Created by patricou on 08/11/2017.
 */
public interface FileDocumentRepository   extends ReactiveMongoRepository<FileDocument, String> {

    @Override
    Flux<FileDocument> findAll();

    // the regex expr allows to do a like
    @Query("{ 'wordLines.word': {$regex : ?0}}")
    Flux<FileDocument> findFileDocumentsByWordLinesWord(String word);

}
