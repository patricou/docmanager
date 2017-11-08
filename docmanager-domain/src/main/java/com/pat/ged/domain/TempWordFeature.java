package com.pat.ged.domain;

/**
 * Created by patricou on 01/11/2017.
 */
public class TempWordFeature {
    private Integer lineNumber;
    private String word;

    public TempWordFeature(Integer line, String word) {
        this.lineNumber = line;
        this.word = word;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return getLineNumber().toString();
    }
}