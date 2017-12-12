package com.pat.ged.controller;

import com.google.common.base.Stopwatch;
import com.pat.ged.domain.FileDocument;
import com.pat.ged.domain.Paragraphs;
import com.pat.ged.domain.WordLines;
import com.pat.ged.exception.FileDocumentException;
import com.pat.ged.repository.FileDocumentRepository;
import com.pat.ged.service.FileDocumentService;
import com.pat.ged.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
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

/**
 * Created by patricou on 08/11/2017.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(   origins = "http://localhost:4200",
                methods = {RequestMethod.GET, RequestMethod.DELETE,RequestMethod.OPTIONS,RequestMethod.POST},
                maxAge = 3600,
                allowCredentials = "true")
@Api(value = "/api", description = "DocManager App Operations API")
public class FileDocumentController {

    @Autowired
    private FileDocumentRepository fileDocumentRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    FileDocumentService fileDocumentService;

    public static final Logger logger = LoggerFactory.getLogger( FileDocumentController.class );

    // save the file in mongodb and compute in which lines are each words ( saved in FilDocument )
    @PostMapping(value="/fileupload" , consumes = "multipart/form-data" )
    @Transactional(propagation = Propagation.REQUIRED)  // doesn't work with MongoDB
    @ApiOperation(value = "Save a file ( PDF, Word or txt in MongoDB.")
    // Important note : the name associate with RequestParam is 'file' --> seen in the browser network request.
    public  Mono<ResponseEntity<FileDocument>> postFileWithFlow(@RequestParam("file") MultipartFile multipartFile, @RequestParam(value = "metadata",required = false) String metaData ) {

        try{

            final Stopwatch stopwatch = Stopwatch.createStarted();

            MultipartFile filedata = multipartFile;

            if (logger.isInfoEnabled()) logger.info("File Name upload : "+ filedata.getOriginalFilename()+" <--> Content/type : " + filedata.getContentType());

            FileDocument fileDocument = new FileDocument(filedata.getOriginalFilename());
            fileDocument.setContentType(filedata.getContentType());
            fileDocument.setCreatedDate(LocalDateTime.now());
            fileDocument.setSize(Long.toString(filedata.getSize()));
            fileDocument.setWordLines(fileDocumentService.wordLinesList(filedata));

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

        }catch(Exception e){
            throw new FileDocumentException(e.getMessage());
        }
    }

    // get the file from mongodb to display it
    @GetMapping( "/file")
    @ApiOperation(value = "Open the file from MongoDB.")
    public Mono<ResponseEntity<InputStreamResource>> getFile(@RequestParam String filename) throws IOException {

        if (logger.isInfoEnabled()) logger.info("File to download : " + filename);

        try {
            GridFsResource gridFsResource = fileService.getResource(filename);

            // this doesn't work for big files; let wait RELEASE version of MONGODB_REACTIVE
            InputStreamResource inputStreamResource = new InputStreamResource(gridFsResource.getInputStream());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(gridFsResource.getContentType()));
            headers.setContentDispositionFormData(filename, filename);
            headers.set("Content-Disposition", "inline; filename =" + filename);
            headers.set("Content-Length", Long.toString(gridFsResource.contentLength()));

            return Mono.just(
                    ResponseEntity.ok()
                    .headers(headers)
                    .body(inputStreamResource)
            );

        } catch (Exception e) {
            if (logger.isInfoEnabled()) logger.info("File to download Exception : " + e.getMessage());
            throw new FileDocumentException(e.getMessage());
        }
    }

    // find paragraph by word
    @GetMapping(value ="paragraph/{word}")
    @ApiOperation(value = "Finds paragraphs in all files containing word.",
            notes = " Paragraph is one line before and one after the line containing word.",
            response = String.class,
            responseContainer = "Flux")
    public Flux<Paragraphs> findParagraphByWord(@ApiParam(value = "word to be found in paragrapf", required = true) @PathVariable(value = "word") String word){

            final Stopwatch stopwatch = Stopwatch.createStarted();

            final String wordlc = word.toLowerCase().trim();

            Flux<Paragraphs> paragraphsFlux = fileDocumentService.getParagraphFromAllFiles(wordlc);

            stopwatch.stop();
            if (logger.isInfoEnabled()) logger.info("Elapsed time for finding paragraphs by word ==> " + stopwatch  );

            return paragraphsFlux;
    }

    // delete file
    @DeleteMapping(value="/delfile", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation(value = "Delete the file and fileDocument in MongoDB.")
    public Mono<ResponseEntity<String>> deleteFile(@RequestParam String fileid)
    {
        try{
            //delete the file stored in MongoDB
            fileService.delResources(fileid);
            //delete the fileDocument Object store in MongoDB
            FileDocument fileDocument = fileDocumentRepository.findFileDocumentsByIdInGrid(fileid).block();
            if (logger.isInfoEnabled()) logger.info("file to delete ==> " + fileDocument + "/ fileid " + fileid + " " );
            return fileDocumentRepository
                    .deleteById(fileDocument.getId())
                    .then(Mono
                        .just(new ResponseEntity<>(HttpStatus.OK)));
        }catch(Exception e){
            throw new FileDocumentException(e.getMessage());
        }
    }

    // get the name of all files
    @GetMapping(value = "/files/{tofind}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Display all files present in the MongoDB.")
    public List<FileDocument> findAllFileDocumentName( @PathVariable(value = "tofind") String tofind )
    {
        if (logger.isInfoEnabled()) logger.info("file to find ==> " + tofind );

        try {

            Flux<FileDocument> fileDocumentFlux = null;
            fileDocumentFlux = "all".equals(tofind) ? fileDocumentRepository.findAll(Sort.by("filename")) : fileDocumentRepository.findFileDocumentsByWordLinesWord(tofind);

            return fileDocumentFlux.map(f -> {
                FileDocument fileDocument = new FileDocument(f.getFilename());
                fileDocument.setId(f.getId());
                fileDocument.setIdInGrid(f.getIdInGrid());
                fileDocument.setContentType(f.getContentType());
                fileDocument.setCreatedDate(f.getCreatedDate());
                fileDocument.setSize(f.getSize());
                return fileDocument;
            }).toStream().collect(Collectors.toList());

        }catch(Exception e){
            throw new FileDocumentException(e.getMessage());
        }
    }

    @ExceptionHandler
    public ResponseEntity handleDocumentException(FileDocumentException ex) {
        if (logger.isInfoEnabled()) logger.info("EXCEPTION : " + ex.getMessage());
        return ResponseEntity.
                status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("FileDocumentException : "+ ex.getMessage());
    }
}
