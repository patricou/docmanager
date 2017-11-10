package com.pat.ged.controller;

import com.google.common.base.Stopwatch;
import com.pat.ged.domain.FileDocument;
import com.pat.ged.domain.Paragraphs;
import com.pat.ged.domain.WordLines;
import com.pat.ged.exception.FileDocumentException;
import com.pat.ged.repository.FileDocumentRepository;
import com.pat.ged.service.FileDocumentService;
import com.pat.ged.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;

import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by patricou on 08/11/2017.
 */
@RestController
@RequestMapping("/api")
public class FileDocumentController {

    @Autowired
    private FileDocumentRepository fileDocumentRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    FileDocumentService fileDocumentService;

    public static final Logger logger = LoggerFactory.getLogger( FileDocumentController.class );

    //use for testing
    @GetMapping("/test")
    public Flux<String> test(){

         return Flux.zip(Flux.fromStream(Stream.of("1","2","3")),Flux.fromStream(Stream.of("6","7"))).map( t-> t.getT1() + t.getT2());

    }

    // save the file in mongodb and compute in which lines are each words ( saved in FilDocument )
    @PostMapping(value="/file" , consumes = "multipart/form-data" )
    @Transactional(propagation = Propagation.REQUIRED)  // doesn't work with MongoDB
    // Important note : the name associate with RequestParam is 'file' --> seen in the browser network request.
    public  Mono<ResponseEntity<FileDocument>> postFileWithFlow(@RequestParam("file") MultipartFile multipartFile, @RequestParam(value = "metadata",required = false) String metaData ) {

        final Stopwatch stopwatch = Stopwatch.createStarted();

        MultipartFile filedata = multipartFile;

        try (InputStream inputStream = filedata.getInputStream()) {

            if (logger.isInfoEnabled()) logger.info("File NAme : "+ filedata.getOriginalFilename()+" <--> Content/type : " + filedata.getContentType());

            FileDocument fileDocument = new FileDocument(filedata.getOriginalFilename());
            fileDocument.setContentType(filedata.getContentType());
            fileDocument.setCreatedDate(LocalDateTime.now());
            fileDocument.setSize(Long.toString(filedata.getSize()));
            fileDocument.setWordLines(fileDocumentService.wordLinesList(inputStream,filedata.getContentType()));

            // add WordLines for metaData passed via @RequestParam
            if (metaData != null  ) {

                if (logger.isInfoEnabled()) logger.info("metaData : " + metaData );
                if (fileDocument.getWordLines() == null) fileDocument.setWordLines( new ArrayList<>() );

                Arrays.stream(metaData.split(" ")).forEach(word -> {
                    Set<Integer> integerSet = new TreeSet<>();
                    // metadata said as in line 0
                    integerSet.add(0);
                    fileDocument.getWordLines().add(0, new WordLines(word.toLowerCase(), integerSet));
                });
            }

            //Save the file itself
            String id = fileService.saveFile(filedata);
            fileDocument.setIdInGrid( id );

            stopwatch.stop(); //optional
            if (logger.isInfoEnabled()) logger.info("Elapsed time ==> " + stopwatch);

            return fileDocumentRepository.save(fileDocument)
                    .map(f -> new ResponseEntity<FileDocument>(f, HttpStatus.CREATED))
                    .switchIfEmpty(Mono.error(new FileDocumentException("Issue with the File save")));

        } catch (Exception e) {
            throw new FileDocumentException(e.getMessage());
        }

    }

    // get the file from mongodb to display it
    @GetMapping( "/file")
    public Mono<ResponseEntity<InputStreamResource>> getFile(@RequestParam String filename) throws IOException {

        try {
            GridFsResource gridFsResource = fileService.getResource(filename);

            // this doesn't work for big files; let wait RELEASE version of MONGODB_REACTIVE
            InputStreamResource inputStreamResource = new InputStreamResource(gridFsResource.getInputStream());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(gridFsResource.getContentType()));
            headers.setContentDispositionFormData(filename, filename);
            headers.set("Content-Disposition", "inline; filename =" + filename);
            headers.set("Content-Length", Long.toString(gridFsResource.contentLength()));

            return Mono.just(ResponseEntity.ok()
                    .headers(headers)
                    .body(inputStreamResource));

        } catch (Exception e) {
            throw new FileDocumentException(e.getMessage());
        }
    }

    // search all files containing the word
    @GetMapping( value = "/file/{word}" , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> searchWordInFileDocument(@PathVariable(value = "word") String word){

        final Stopwatch stopwatch = Stopwatch.createStarted();

        final String wordlc = word.toLowerCase().trim();

        if (logger.isInfoEnabled()) logger.info("String to Search : " + wordlc);

        Flux<FileDocument> fileDocumentlist =  fileDocumentRepository.findFileDocumentsByWordLinesWord(wordlc);

        Flux<String> stringFlux =  fileDocumentlist
                .map(fileDocument -> ""+fileDocument.toString() + " / lines : " +
                        fileDocument.getWordLines()
                                .stream()
                                .filter(f->f.getWord().contains(wordlc) )
                                .map(f-> ""+f.getLinesNumber())
                                .collect(Collectors.joining(" / ")));

        stopwatch.stop();
        if (logger.isInfoEnabled()) logger.info("Elapsed time for finding lines containing the word ==> " + stopwatch);

        return stringFlux.switchIfEmpty(Flux.error(new FileDocumentException(word + " Not Found")));
    }

    // find paragraph by word
    @GetMapping(value ="paragraph/{word}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Paragraphs> findParagraphByWord(@PathVariable(value = "word") String word){

        final Stopwatch stopwatch = Stopwatch.createStarted();

        final String wordlc = word.toLowerCase().trim();

        Flux<Paragraphs> paragraphsFlux = fileDocumentService.getParagraphFromAllFiles(wordlc);

        stopwatch.stop();
        if (logger.isInfoEnabled()) logger.info("Elapsed time for finding paragraphs by word ==> " + stopwatch  );

        return paragraphsFlux.switchIfEmpty(Flux.error(new FileDocumentException(word + " Not Found")));
    }

    // delete all files
    @DeleteMapping("/delallfiles")
    public Mono<ResponseEntity<Void>> deleteAllFileDocument()
    {
        return fileDocumentRepository.deleteAll().then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)));
    }

    // get the name of all files
    @GetMapping(value = "/files", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> findAllFileDocumentName()
    {
        return fileDocumentRepository.findAll().map(f->" "+f.getFilename()+" : "+f.getContentType() +" : "+f.getSize());
    }

    @ExceptionHandler
    public ResponseEntity handleDocumentException(FileDocumentException ex) {

        if (logger.isInfoEnabled()) logger.info("FileDocumentException : " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("FileDocumentException : "+ ex.getMessage());
    }
}
