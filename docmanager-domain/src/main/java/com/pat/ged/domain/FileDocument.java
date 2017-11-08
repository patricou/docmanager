package com.pat.ged.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by patricou on 02/11/2017.
 */
@Document(collection = "filesdocuments")
public class FileDocument {

    @Id
    private String id;
    @NotNull
    private String idInGrid;
    @NotNull
    private String filename;
    @NotNull
    private String contentType;
    @NotNull
    private LocalDateTime createdDate;
    private String owner;
    @NotNull
    private String size;
    private List<WordLines> wordLines;
    @Override
    public String toString() {
        return getFilename();
    }

    public FileDocument(@NotNull String filename) {
        this.filename = filename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdInGrid() {
        return idInGrid;
    }

    public void setIdInGrid(String idInGrid) {
        this.idInGrid = idInGrid;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public List<WordLines> getWordLines() {
        return wordLines;
    }

    public void setWordLines(List<WordLines> wordLines) {
        this.wordLines = wordLines;
    }
}
