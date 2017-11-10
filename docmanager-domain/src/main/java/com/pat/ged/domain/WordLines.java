package com.pat.ged.domain;

import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Created by patricou on 03/11/2017.
 */
@Document(collection = "wordlines")
public class WordLines {

    @NotNull
    private String word;
    private Set<Integer> linesNumber;

    public Set<Integer> getLinesNumber() {
        return linesNumber;
    }

    public void setLinesNumber(Set<Integer> linesNumber) {
        this.linesNumber = linesNumber;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public WordLines( String word, Set<Integer> linesNumber) {
        this.linesNumber = linesNumber;
        this.word = word;
    }

    @Override
    public String toString() {
        return "WordLines{" +
                "word='" + word + '\'' +
                ", linesNumber=" + linesNumber +
                '}';
    }
}
