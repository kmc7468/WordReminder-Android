package com.staticom.wordreminder.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Tag {

    private String tag;
    private final Vocabulary vocabulary;

    private final List<Word> words = new ArrayList<>();
    private final List<Meaning> meanings = new ArrayList<>();

    public Tag(String tag, Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
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

    public void sortMeanings() {
        meanings.sort((a, b) -> {
            if (a.getWord() == b.getWord()) {
                final List<Meaning> meanings = a.getWord().getMeanings();

                return Integer.compare(meanings.indexOf(a), meanings.indexOf(b));
            } else {
                final List<Word> words = vocabulary.getWords();

                return Integer.compare(words.indexOf(a.getWord()), words.indexOf(b.getWord()));
            }
        });
    }
}