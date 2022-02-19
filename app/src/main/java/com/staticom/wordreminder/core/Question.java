package com.staticom.wordreminder.core;

public class Question {

    private final QuestionType type;
    private final Meaning answer;

    private final Meaning[] choices;

    public Question(QuestionType type, Meaning answer, Meaning[] choices) {
        this.type = type;
        this.answer = answer;

        this.choices = choices;
    }

    public QuestionType getType() {
        return type;
    }

    public Meaning getAnswer() {
        return answer;
    }

    public Meaning[] getChoices() {
        return choices;
    }

    public Meaning getChoice(int index) {
        return choices[index];
    }
}