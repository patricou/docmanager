package com.pat.ged.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patricou on 06/11/2017.
 */
public class Paragraphs {

    private String fromFile;
    private String fromFileID;

    private List<ParagraphElement> paragraphElements = new ArrayList<>(10);

    public String getFromFile() {
        return fromFile;
    }

    public void setFromFile(String fromFile) {
        this.fromFile = fromFile;
    }

    public String getFromFileID() {
        return fromFileID;
    }

    public void setFromFileID(String fromFileID) {
        this.fromFileID = fromFileID;
    }

    public List<ParagraphElement> getParagraphElements() {
        return paragraphElements;
    }

    public void setParagraphElements(List<ParagraphElement> paragraphElements) {
        this.paragraphElements = paragraphElements;
    }

    public Paragraphs(String fromFile, String fromFileID) {
        this.fromFile = fromFile;
        this.fromFileID = fromFileID;
    }

    @Override
    public String toString() {
        return "Paragraphs{" +
                "fromFile='" + fromFile + '\'' +
                ", paragraphElements=" + paragraphElements +
                '}';
    }
}
