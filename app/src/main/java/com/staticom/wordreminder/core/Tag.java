package com.staticom.wordreminder.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Tag implements Serializable {

    private final Vocabulary vocabulary;
    private String tag;

    private final List<Word> words = new ArrayList<>();
    private final List<Meaning> meanings = new ArrayList<>();

    public Tag(Vocabulary vocabulary, String tag) {
        this.vocabulary = vocabulary;
        this.tag = tag;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<Word> getWords() {
        return Collections.unmodifiableList(words);
    }

    public void sortWords() {
        words.sort(Comparator.comparingInt(word -> vocabulary.getWords().indexOf(word)));
    }

    public List<Meaning> getMeanings() {
        return Collections.unmodifiableList(meanings);
    }

    public void addMeaning(Meaning meaning) {
        meanings.add(meaning);

        final Word word = meaning.getWord();
        if (!words.contains(word)) {
            words.add(word);
        }
    }

    public void removeMeaning(Meaning meaning) {
        meanings.remove(meaning);

        for (final Meaning m : meanings) {
            if (m.getWord() == meaning.getWord()) return;
        }

        words.remove(meaning.getWord());
    }

    public Vocabulary makeVocabulary() {
        final Vocabulary vocabulary = new Vocabulary();

        vocabulary.addTag(this);

        sortWords();

        for (final Word word : words) {
            final Word wordRef = new Word(word.getWord());

            for (final Meaning meaning : word.getMeanings()) {
                if (meaning.containsTag(this)) {
                    wordRef.addMeaningRef(meaning);
                }
            }

            vocabulary.addWord(wordRef);
        }

        return vocabulary;
    }
}