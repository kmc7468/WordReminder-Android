package com.staticom.wordreminder.core;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Vocabulary {

    private String name;
    private String path;
    private LocalDateTime time;

    private final List<Word> words = new ArrayList<>();

    public Vocabulary(String name) {
        this.name = name;
        this.path = UUID.randomUUID().toString() + ".kv";
        this.time = LocalDateTime.now();
    }

    private Vocabulary(String name, String path, LocalDateTime time) {
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public List<Word> getWords() {
        return Collections.unmodifiableList(words);
    }

    public Word getWord(int index) {
        return words.get(index);
    }

    public int indexOfWord(String word) {
        for (int i = 0; i < words.size(); ++i) {
            if (words.get(i).getWord().equals(word)) return i;
        }

        return -1;
    }

    public void addWord(Word word) {
        word.setVocabulary(this);

        words.add(word);
    }

    public void addWordRef(Word word) {
        words.add(word);
    }

    public void removeWord(Word word) {
        if (word.getVocabulary() == this) {
            word.setVocabulary(null);
        }

        words.remove(word);
    }
}