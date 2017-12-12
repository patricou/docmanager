package com.pat.ged.service;
import com.pat.ged.domain.*;
import com.pat.ged.exception.FileDocumentException;
import com.pat.ged.repository.FileDocumentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import javax.xml.bind.JAXBElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by patricou on 08/11/2017.
 */
@Service
public class FileDocumentService {

    @Autowired
    FileDocumentRepository fileDocumentRepository;

    @Autowired
    FileService fileService;

    private static final Logger logger = LoggerFactory.getLogger( FileDocumentService.class );

    // return the list of all lines where a word is present
    public List<WordLines> getWordsLines(Flux<String> linesOfString){

        List<WordLines> wordsLines = new ArrayList<>();

         Flux.zip(Flux.range(1, Integer.MAX_VALUE), linesOfString)
                .flatMap(tuple ->  Flux.fromArray(tuple.getT2().split("[^a-zA-Zéàèç]"))
                                        .map(String::toLowerCase)
                                        .filter(word -> word.length() > 2 && word.matches("[a-zéàèç]+"))
                                        .map(w -> new TempWordFeature(tuple.getT1(), w ))
                )
                .toStream()
                // Gather the data in a TreeMap<String ( is the word itself ) ,TreeSet<Integer> ( is the list of Lines) >
                .collect(Collectors.groupingBy(
                TempWordFeature::getWord,
                TreeMap::new,
                Collectors.mapping(
                        TempWordFeature::getLineNumber,
                        Collectors.toCollection(TreeSet::new)))
        )
                // add the Map<String, Set<Integer> in a List<WordLines>
                .forEach( (k, v) -> wordsLines.add(new WordLines(k ,v)));

         return wordsLines;
    }

    //  return text of the PDF document
    private String getTextFromPDFDoc (InputStream inputStream){

        String pdfText = "No Text found";
        try (PDDocument document = PDDocument.load(inputStream)) {
            document.getClass();
            if (!document.isEncrypted()) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);
                PDFTextStripper tStripper = new PDFTextStripper();
                pdfText = tStripper.getText(document);
            }
        }
        catch ( Exception e){
            throw new FileDocumentException("Issue in PDF conversion " + e.getMessage());
        }
        return pdfText;
    }

    // for word extraction
    private  List<Object> getAllelementObjects(Object obj,Class<?> toSearch) {
        List<Object> result = new ArrayList<>();
        if (obj instanceof JAXBElement)
            obj = ((JAXBElement<?>) obj).getValue();
        if (obj.getClass().equals(toSearch))
            result.add(obj);
        else if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllelementObjects(child, toSearch));
            }
        }
        return result;
    }

    // return the list of each line of the word document
    private List<String> retrieveWordDocumentsLines( InputStream inputStream ) {

        List<Object> texts = null;
        try {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(inputStream);
            texts = getAllelementObjects(wordMLPackage.getMainDocumentPart(), Text.class);
        }catch (Docx4JException e){
            throw new FileDocumentException("Issue in Word conversion " + e.getMessage());
        }
        return texts.stream().map(x->((Text)x).getValue()).collect(Collectors.toList());
    }

    // return the List of paragraphs containing the word
    public Flux<Paragraphs> getParagraphFromAllFiles (String word){

        if (logger.isInfoEnabled()) logger.info("getParagraphFromAllFiles "+ word);

        return fileDocumentRepository
            .findFileDocumentsByWordLinesWord(word)
            .map(fileDocument -> {
                        Paragraphs paragraphs = new Paragraphs(fileDocument.getFilename(), fileDocument.getIdInGrid());
                        List<ParagraphElement> paragraphElements = readParagraph(fileDocument, word);
                        paragraphs.setParagraphElements(paragraphElements);
                        return paragraphs;
                }
            ).doOnError(c -> {
                    if (logger.isInfoEnabled()) logger.info("ERROR .......... " + c.getMessage());
                    throw new FileDocumentException("Exception when retrieve in Paragraphs : " + c.getMessage());
                });
    }

    private List<ParagraphElement> getListParagraphElement(Flux<String> lines, FileDocument fileDocument, String word ){

        Integer delta = 2;
        Set<Integer> linesToRead = fileDocument.getWordLines()
                .stream()
                .filter(wordLines ->wordLines.getWord().contains(word))
                .flatMap(wordLines -> wordLines.getLinesNumber().stream())
                .flatMapToInt(lineNumber-> IntStream.range(lineNumber-delta,lineNumber+delta+1))
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toSet());

        return  Flux.zip(Flux.range(1, Integer.MAX_VALUE), lines)
                    .filter ( tuple -> linesToRead.contains(tuple.getT1()))
                    .map( tuple -> new ParagraphElement( tuple.getT1(), tuple.getT2()))
                    .collectList()
                    .block();
    }

    // return the List of Paragraph containing word
    private List<ParagraphElement> readParagraph(FileDocument fileDocument, String word){

        GridFsResource gridFsResource = fileService.getResource(fileDocument.getFilename());

        try(InputStream inputStream = gridFsResource.getInputStream()){
            String contentType = gridFsResource.getContentType();

            // Text format
            if (contentType.contains("text/plain")) {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                return getListParagraphElement(Flux.fromStream(br.lines()),fileDocument, word);
            }
            // Word format
            else if (contentType.contains("application/vnd.openxmlformats-officedocument.word")) {
                return getListParagraphElement(Flux.fromIterable(retrieveWordDocumentsLines(inputStream)),fileDocument, word);
            }
            // PDF format
            else if (contentType.contains("application/pdf")) {
                // Get Text from PDF document
                String pdfFileInText = getTextFromPDFDoc(inputStream);
                // split each lines by whitespace and get the number of lines by words
                return getListParagraphElement(Flux.fromArray(pdfFileInText.split("\\r?\\n")),fileDocument, word);
            }

        }catch (IOException e){
            throw new FileDocumentException("Error in FileDocumentService.readParagraph() "+ e.getMessage());
        }
        return new ArrayList<>();
    }

    // return a list of lines by word (  used when save of FileDocument )
    public List<WordLines> wordLinesList(MultipartFile fileData){

        try (InputStream inputStream = fileData.getInputStream()) {
            String contentType = fileData.getContentType();
            // Text format
            if (contentType.contains("text/plain")) {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                return getWordsLines(Flux.fromStream(br.lines()));
            }
            // Word format
            else if (contentType.contains("application/vnd.openxmlformats-officedocument.word")) {
                return getWordsLines(Flux.fromIterable(retrieveWordDocumentsLines(inputStream)));
            }
            // PDF format
            else if (contentType.contains("application/pdf")) {
                // Get Text from PDF document
                String pdfFileInText = getTextFromPDFDoc(inputStream);
                // split each lines by whitespace and get the number of lines by words
                return getWordsLines(Flux.fromArray(pdfFileInText.split("\\r?\\n")));
            }
            // jpeg Format
            else if (contentType.contains("image/jpeg")) {
                if (logger.isInfoEnabled()) logger.info("Content-type : image/jpeg");
                return new ArrayList<>();
            }
            // Other Format
            else
                throw new FileDocumentException("'" + contentType + "' Content type not yet implemented");
        } catch (Exception e) {
            throw new FileDocumentException(e.getMessage());
        }
    }

}


