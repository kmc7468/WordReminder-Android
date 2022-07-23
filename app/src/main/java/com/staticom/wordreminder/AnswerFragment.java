package com.staticom.wordreminder;

import com.staticom.wordreminder.core.Question;
import com.staticom.wordreminder.core.QuestionContext;

public interface AnswerFragment {

    void restoreQuestion(QuestionContext context, Question question);

    boolean isEmptyAnswer();

    boolean isCorrectAnswer();
}