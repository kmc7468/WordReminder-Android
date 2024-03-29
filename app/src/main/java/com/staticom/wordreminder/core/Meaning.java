package com.staticom.wordreminder.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Meaning implements Serializable {

    public enum Component {
        WORD,
        MEANING,
        PRONUNCIATION,
        EXAMPLE,
    }

    private Word word;
    private String meaning;
    private String pronunciation;
    private String example = "";

    private final List<Tag> tags = new ArrayList<>();

    public Meaning(String meaning, String pronunciation) {
        this.meaning = meaning;
        this.pronunciation = pronunciation;
    }

    public Meaning(String meaning, String pronunciation, String example) {
        this.meaning = meaning;
        this.pronunciation = pronunciation;
        this.example = example;
    }

    public Meaning(Word word, String meaning, String pronunciation, String example) {
        this.word = word;
        this.meaning = meaning;
        this.pronunciation = pronunciation;
        this.example = example;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public boolean hasPronunciation() {
        return !pronunciation.isEmpty() && !pronunciation.equals(word.getWord());
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public boolean hasExample() {
        return !example.isEmpty();
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getComponent(Component component) {
        switch (component) {
            case WORD:
                return word.getWord();

            case MEANING:
                return meaning;

            case PRONUNCIATION:
                return pronunciation;

            case EXAMPLE:
                return example;

            default:
                return null;
        }
    }

    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public boolean containsTag(Tag tag) {
        return tags.contains(tag);
    }

    public boolean containsTag(List<Tag> tags) {
        for (final Tag tag : tags) {
            if (containsTag(tag)) return true;
        }

        return false;
    }

    public void addTag(Tag tag) {
        tags.add(tag);

        tag.addMeaning(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);

        tag.removeMeaning(this);
    }

    public void removeAllTags() {
        for (final Tag tag : tags) {
            tag.removeMeaning(this);
        }

        tags.clear();
    }
}