package com.staticom.wordreminder.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Word implements Serializable {

    private Vocabulary vocabulary;
    private String word;
    private final List<Meaning> meanings = new ArrayList<>();

    public Word(String word) {
        this.word = word;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<Meaning> getMeanings() {
        return Collections.unmodifiableList(meanings);
    }

    public Meaning getMeaning(int index) {
        return meanings.get(index);
    }

    public boolean containsMeaning(String meaning) {
        return findMeaning(meaning) != null;
    }

    public Meaning findMeaning(String meaning) {
        for (final Meaning m : meanings) {
            if (m.getMeaning().equals(meaning)) return m;
        }

        return null;
    }

    public Meaning addMeaning(Meaning meaning) {
        meaning.setWord(this);

        meanings.add(meaning);

        return meaning;
    }

    public void addMeaningRef(Meaning meaning) {
        meanings.add(meaning);
    }

    public void removeMeaning(int index) {
        final Meaning meaning = meanings.remove(index);

        if (meaning.getWord() == this) {
            meaning.setWord(null);
        }
    }

    public void removeMeaning(Meaning meaning) {
        if (meaning.getWord() == this) {
            meaning.setWord(null);
        }

        meanings.remove(meaning);
    }

    public boolean hasExample() {
        for (final Meaning meaning : meanings) {
            if (meaning.hasExample()) return true;
        }

        return false;
    }

    public Meaning mergeMeanings() {
        if (meanings.size() == 1) return getMeaning(0);

        final List<String> meanings = new ArrayList<>();
        final List<String> pronunciations = new ArrayList<>();

        for (final Meaning meaning : this.meanings) {
            meanings.add(meaning.getMeaning());

            if (meaning.hasPronunciation()) {
                pronunciations.add(meaning.getPronunciation());
            }
        }

        final String mergedMeanings = String.join(", ", meanings);
        final String mergedPronunciations = String.join(", ", makeUnique(pronunciations));

        return new Meaning(this, mergedMeanings, mergedPronunciations);
    }

    private static List<String> makeUnique(List<String> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }
}