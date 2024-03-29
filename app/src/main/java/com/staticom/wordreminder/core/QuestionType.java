package com.staticom.wordreminder.core;

import android.content.Context;

import androidx.annotation.StringRes;

import com.staticom.wordreminder.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class QuestionType implements Serializable {

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

    @StringRes
    private final int messageId;
    private final Meaning.Component mainComponent, answerComponent;
    private final EnumSet<Meaning.Component> hintsForMainComponent, hintsForAnswerComponent;

    private final List<Meaning> usedMeanings = new ArrayList<>();

    protected QuestionType(Type type, AnswerType answerType,
                           @StringRes int messageId, Meaning.Component mainComponent, EnumSet<Meaning.Component> hintsForMainComponent,
                           Meaning.Component answerComponent, EnumSet<Meaning.Component> hintsForAnswerComponent) {
        this.type = type;
        this.answerType = answerType;

        this.messageId = messageId;
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

    public String getEmptyAnswerMessage(Context context) {
        if (answerType == AnswerType.MULTIPLE_CHOICE)
            return context.getString(R.string.question_type_multiple_choice_error_empty_answer);
        else if (answerType == AnswerType.SHORT_ANSWER)
            return context.getString(R.string.question_type_short_answer_error_empty_answer);
        else return null;
    }

    public String getMessage(Context context) {
        return context.getString(messageId);
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

    public String getAnswerComponent(Meaning meaning) {
        return meaning.getComponent(answerComponent);
    }

    public boolean shouldDisplayPronunciationForAnswerComponent(Meaning meaning) {
        return meaning.hasPronunciation() && hintsForAnswerComponent.contains(Meaning.Component.PRONUNCIATION);
    }

    public boolean shouldDisplayExampleForAnswerComponent(Meaning meaning) {
        return meaning.hasExample() && hintsForAnswerComponent.contains(Meaning.Component.EXAMPLE);
    }

    public List<Meaning> getUsedMeanings() {
        return Collections.unmodifiableList(usedMeanings);
    }

    public void addUsedMeaning(Meaning meaning) {
        usedMeanings.add(meaning);
    }

    public void removeAllUsedMeanings() {
        usedMeanings.clear();
    }

    public boolean isUsableForAnswer(Meaning meaning) {
        return true;
    }

    public boolean isDuplicatedForAnswer(Meaning fixedMeaning, Meaning meaning) {
        if (fixedMeaning.getWord() == meaning.getWord() ||
                fixedMeaning.getMeaning().equals(meaning.getMeaning())) return true;

        final Word fixedWord = fixedMeaning.getWord();

        return fixedWord.containsMeaning(meaning.getMeaning());
    }

    public static class WordToMeaning extends QuestionType {

        private WordToMeaning(AnswerType answerType) {
            super(Type.WORD_TO_MEANING, answerType,
                    R.string.question_type_word_to_meaning_message, Meaning.Component.WORD, EnumSet.of(Meaning.Component.PRONUNCIATION, Meaning.Component.EXAMPLE),
                    Meaning.Component.MEANING, EnumSet.noneOf(Meaning.Component.class));
        }

        public static WordToMeaning multipleChoice() {
            return new WordToMeaning(AnswerType.MULTIPLE_CHOICE);
        }

        public static WordToMeaning shortAnswer() {
            return new WordToMeaning(AnswerType.SHORT_ANSWER);
        }
    }

    public static class MeaningToWord extends QuestionType {

        private MeaningToWord(AnswerType answerType) {
            super(Type.MEANING_TO_WORD, answerType,
                    R.string.question_type_meaning_to_word_message, Meaning.Component.MEANING, EnumSet.noneOf(Meaning.Component.class),
                    Meaning.Component.WORD, EnumSet.of(Meaning.Component.PRONUNCIATION, Meaning.Component.EXAMPLE));
        }

        public static MeaningToWord multipleChoice() {
            return new MeaningToWord(AnswerType.MULTIPLE_CHOICE);
        }

        public static MeaningToWord shortAnswer() {
            return new MeaningToWord(AnswerType.SHORT_ANSWER);
        }
    }
}