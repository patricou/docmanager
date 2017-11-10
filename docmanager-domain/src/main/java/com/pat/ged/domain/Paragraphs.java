package com.pat.ged.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patricou on 06/11/2017.
 */
public class Paragraphs {

    private String fromFile;

    private List<ParagraphElement> paragraphElements = new ArrayList<>(10);

    public String getFromFile() {
        return fromFile;
    }

    public void setFromFile(String fromFile) {
        this.fromFile = fromFile;
    }


    public List<ParagraphElement> getParagraphElements() {
        return paragraphElements;
    }

    public void setParagraphElements(List<ParagraphElement> paragraphElements) {
        this.paragraphElements = paragraphElements;
    }

    public Paragraphs(String fromFile) {
        this.fromFile = fromFile;

    }
}
