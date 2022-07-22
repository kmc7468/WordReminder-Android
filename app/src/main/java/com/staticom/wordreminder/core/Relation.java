package com.staticom.wordreminder.core;

import java.io.Serializable;

public class Relation implements Serializable {

    private Word word;
    private String relation;

    public Relation(Word word, String relation) {
        this.word = word;
        this.relation = relation;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
