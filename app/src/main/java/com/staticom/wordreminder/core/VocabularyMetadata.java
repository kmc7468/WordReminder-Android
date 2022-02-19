package com.staticom.wordreminder.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class VocabularyMetadata {

    private String name;
    private Path path;
    private LocalDateTime time;

    private Vocabulary vocabulary = null;

    public VocabularyMetadata(String name, Path path, LocalDateTime time) {
        this.name = name;
        this.path = path;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public boolean hasVocabulary() {
        return vocabulary != null;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void saveVocabulary() throws IOException {
        try (final FileOutputStream stream = new FileOutputStream(path.toFile())) {
            vocabulary.writeToFileStream(stream);
        }
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public void loadVocabulary() throws IOException {
        try (final FileInputStream stream = new FileInputStream(path.toFile())) {
            vocabulary = Vocabulary.readFromFileStream(stream);
        }
    }
}