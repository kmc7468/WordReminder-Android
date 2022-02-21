package com.staticom.wordreminder.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class VocabularyMetadata {

    private static class SerializableVocabularyMetadata implements Serializable {

        private final String name;
        private final String path;
        private final LocalDateTime time;

        private final Vocabulary vocabulary;
        private final boolean shouldSave;

        public SerializableVocabularyMetadata(VocabularyMetadata vocabulary) {
            name = vocabulary.name;
            path = vocabulary.path.toString();
            time = vocabulary.time;

            this.vocabulary = vocabulary.vocabulary;
            shouldSave = vocabulary.shouldSave;
        }
    }

    private String name;
    private Path path;
    private LocalDateTime time;

    private Vocabulary vocabulary;
    private boolean shouldSave = false;

    public VocabularyMetadata(String name, Path path, LocalDateTime time) {
        this.name = name;
        this.path = path;
        this.time = time;
    }

    public VocabularyMetadata(String name, Vocabulary vocabulary) {
        this.name = name;
        this.vocabulary = vocabulary;
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

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public void saveVocabulary() throws IOException {
        try (final FileOutputStream stream = new FileOutputStream(path.toFile())) {
            vocabulary.writeToFileStream(stream);
        }
    }

    public void loadVocabulary() throws IOException {
        try (final FileInputStream stream = new FileInputStream(path.toFile())) {
            vocabulary = Vocabulary.readFromFileStream(stream);
        }
    }

    public boolean shouldSave() {
        return shouldSave;
    }

    public void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public Serializable serialize() {
        return new SerializableVocabularyMetadata(this);
    }

    public static VocabularyMetadata deserialize(Serializable serializable) {
        final SerializableVocabularyMetadata vocabulary = (SerializableVocabularyMetadata)serializable;
        final VocabularyMetadata result = new VocabularyMetadata(
                vocabulary.name, Paths.get(vocabulary.path), vocabulary.time);

        result.vocabulary = vocabulary.vocabulary;
        result.shouldSave = vocabulary.shouldSave;

        return result;
    }
}