package com.staticom.wordreminder.core;

import java.util.EnumSet;

public class QuestionType {

    public enum Type {
        WORD_TO_MEANING,
        MEANING_TO_WORD,
    }

    public enum AnswerType {
        MULTIPLE_CHOICE,
        SHORT_ANSWER,
    }

    private final Type type;
    private final AnswerType answerType;

    private final String message;
    private final Meaning.Component mainComponent;
    private final EnumSet<Meaning.Component> hintsForMainComponent;
    private final Meaning.Component answerComponent;
    private final EnumSet<Meaning.Component> hintsForAnswerComponent;

    protected QuestionType(Type type, AnswerType answerType,
                           String message, Meaning.Component mainComponent, EnumSet<Meaning.Component> hintsForMainComponent,
                           Meaning.Component answerComponent, EnumSet<Meaning.Component> hintsForAnswerComponent) {
        this.type = type;
        this.answerType = answerType;

        this.message = message;
        this.mainComponent = mainComponent;
        this.hintsForMainComponent = hintsForMainComponent;
        this.answerComponent = answerComponent;
        this.hintsForAnswerComponent = hintsForAnswerComponent;
    }

    public Type getType() {
        return type;
    }

    public AnswerType getAnswerType() {
        return answerType;
    }

    public String getMessage() {
        return message;
    }

    public String getMainComponent(Meaning meaning) {
        return meaning.getComponent(mainComponent);
    }

    public boolean shouldDisplayPronunciationForMainComponent(Meaning meaning) {
        return meaning.hasPronunciation() && hintsForMainComponent.contains(Meaning.Component.PRONUNCIATION);
    }

    public boolean shouldDisplayExampleForMainComponent(Meaning meaning) {
        return meaning.hasExample() && hintsForMainComponent.contains(Meaning.Component.EXAMPLE);
    }

    public boolean shouldDisplayPronunciationForAnswerComponent(Meaning meaning) {
        return meaning.hasPronunciation() && hintsForAnswerComponent.contains(Meaning.Component.PRONUNCIATION);
    }

    public boolean shouldDisplayExampleForAnswerComponent(Meaning meaning) {
        return meaning.hasExample() && hintsForAnswerComponent.contains(Meaning.Component.EXAMPLE);
    }
}