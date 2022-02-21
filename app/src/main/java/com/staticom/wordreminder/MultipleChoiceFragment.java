package com.staticom.wordreminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.fragment.app.Fragment;

import com.staticom.wordreminder.core.Question;

public class MultipleChoiceFragment extends Fragment implements AnswerFragment {

    private Question question;

    private final RadioButton[] choices = new RadioButton[5];

    public MultipleChoiceFragment() {
    }

    public MultipleChoiceFragment(Question question) {
        this.question = question;
    }

    private void updateChoicesText() {
        if (question == null) return;

        for (int i = 0; i < 5; ++i) {
            choices[i].setText(question.getType().getAnswerComponent(question.getChoice(i)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_multiple_choice, container, false);

        choices[0] = view.findViewById(R.id.choice0);
        choices[1] = view.findViewById(R.id.choice1);
        choices[2] = view.findViewById(R.id.choice2);
        choices[3] = view.findViewById(R.id.choice3);
        choices[4] = view.findViewById(R.id.choice4);

        updateChoicesText();

        return view;
    }

    @Override
    public boolean isEmptyAnswer() {
        for (final RadioButton choice : choices) {
            if (choice.isChecked()) return false;
        }

        return true;
    }

    @Override
    public boolean isCorrectAnswer() {
        for (int i = 0; i < 5; ++i) {
            if (choices[i].isChecked())
                return question.getAnswer() == question.getChoice(i);
        }

        return false;
    }
}