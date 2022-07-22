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
    private final List<Relation> relations = new ArrayList<>();

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

    private static List<String> makeUnique(List<String> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }

    public Meaning mergeMeanings() {
        return mergeMeanings(", ", ", ", ", ");
    }

    public Meaning mergeMeanings(String meaningDelimiter, String pronunciationDelimiter, String exampleDelimiter) {
        if (meanings.size() == 1) return getMeaning(0);

        final List<String> meanings = new ArrayList<>();
        final List<String> pronunciations = new ArrayList<>();
        final List<String> examples = new ArrayList<>();

        for (final Meaning meaning : this.meanings) {
            meanings.add(meaning.getMeaning());

            if (meaning.hasPronunciation()) {
                pronunciations.add(meaning.getPronunciation());
            }

            if (meaning.hasExample()) {
                examples.add(meaning.getExample());
            }
        }

        final String mergedMeanings = String.join(", ", meanings);
        final String mergedPronunciations = String.join(", ", makeUnique(pronunciations));
        final String mergedExamples = String.join(", ", makeUnique(examples));

        return new Meaning(this, mergedMeanings, mergedPronunciations, mergedExamples);
    }

    public boolean hasRelation() {
        return !relations.isEmpty();
    }

    public boolean containsRelation(Word word) {
        return findRelation(word) != null;
    }

    public Relation findRelation(Word word) {
        for (final Relation relation : relations) {
            if (relation.getWord().getWord().equals(word.getWord())) return relation;
        }

        return null;
    }

    public Relation getRelation(int index) {
        return relations.get(index);
    }

    public List<Relation> getRelations() {
        return Collections.unmodifiableList(relations);
    }

    public void addRelation(Word word, String relation) {
        relations.add(new Relation(word, relation));
    }

    public void removeRelation(int index) {
        relations.remove(index);
    }

    public void removeRelation(Word word) {
        relations.removeIf(relation -> relation.getWord().getWord().equals(word.getWord()));
    }
}