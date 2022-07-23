package com.staticom.wordreminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Question;
import com.staticom.wordreminder.core.QuestionContext;
import com.staticom.wordreminder.core.QuestionType;

public class MultipleChoiceFragment extends Fragment implements AnswerFragment {

    private QuestionContext context;
    private Question question;

    private final RadioButton[] choices = new RadioButton[5];

    public MultipleChoiceFragment() {
    }

    public MultipleChoiceFragment(QuestionContext context, Question question) {
        this.context = context;
        this.question = question;
    }

    private void updateChoicesText() {
        if (question == null) return;

        final QuestionType type = question.getType();

        for (int i = 0; i < 5; ++i) {
            final Meaning choice = question.getChoice(i);
            final StringBuilder textBuilder = new StringBuilder();

            textBuilder.append(type.getAnswerComponent(choice));

            if (context.shouldDisplayPronunciation() && type.shouldDisplayPronunciationForAnswerComponent(choice)) {
                textBuilder.append("<br><small>");
                textBuilder.append(String.format(
                        getString(R.string.question_activity_hint_pronunciation), choice.getPronunciation()));
                textBuilder.append("</small>");
            }

            if (context.shouldDisplayExample() && type.shouldDisplayExampleForAnswerComponent(choice)) {
                textBuilder.append("<br><small>");
                textBuilder.append(String.format(
                        getString(R.string.question_activity_hint_example), choice.getExample()));
                textBuilder.append("</small>");
            }

            choices[i].setText(HtmlCompat.fromHtml(textBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
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
    public void restoreQuestion(QuestionContext context, Question question) {
        this.context = context;
        this.question = question;
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