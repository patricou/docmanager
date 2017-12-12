package com.pat.ged.domain;

/**
 * Created by patricou on 06/11/2017.
 */
public class ParagraphElement {

    private Integer line;
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public ParagraphElement(Integer line, String text) {
        this.text = text;
        this.line = line;
    }

    @Override
    public String toString() {
        return "ParagraphElement{" +
                "line=" + line +
                ", text='" + text + '\'' +
                '}';
    }
}