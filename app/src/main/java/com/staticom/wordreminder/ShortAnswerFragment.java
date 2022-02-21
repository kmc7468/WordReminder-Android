package com.staticom.wordreminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Question;
import com.staticom.wordreminder.core.QuestionType;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.Word;

public class ShortAnswerFragment extends Fragment implements AnswerFragment {

    private Question question;

    private EditText answer;

    public ShortAnswerFragment() {
    }

    public ShortAnswerFragment(Question question) {
        this.question = question;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_short_answer, container, false);

        answer = view.findViewById(R.id.answer);

        return view;
    }

    @Override
    public boolean isEmptyAnswer() {
        return answer.getText().toString().trim().isEmpty();
    }

    @Override
    public boolean isCorrectAnswer() {
        final QuestionType type = question.getType();
        final String answer = this.answer.getText().toString().toLowerCase().trim();

        if (type.getAnswerComponent(question.getAnswer()).toLowerCase().equals(answer)) return true;

        if (type.getType() == QuestionType.Type.MEANING_TO_WORD) {
            final Vocabulary vocabulary = question.getVocabulary();
            for (final Word word : vocabulary.getWords()) {
                for (final Meaning meaning : word.getMeanings()) {
                    if (type.getAnswerComponent(meaning).toLowerCase().equals(answer)) return true;
                }
            }
        }

        return false;
    }
}