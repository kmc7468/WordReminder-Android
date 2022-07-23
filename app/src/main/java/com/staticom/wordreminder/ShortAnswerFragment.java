package com.staticom.wordreminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Question;
import com.staticom.wordreminder.core.QuestionContext;
import com.staticom.wordreminder.core.QuestionType;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.Word;

public class ShortAnswerFragment extends Fragment implements AnswerFragment {

    private QuestionContext context;
    private Question question;

    private EditText answer;
    private TextView hint;

    public ShortAnswerFragment() {
    }

    public ShortAnswerFragment(QuestionContext context, Question question) {
        this.context = context;
        this.question = question;
    }

    private void updateHintText() {
        if (question == null) return;

        final QuestionType type = question.getType();
        final Meaning answer = question.getAnswer();

        final StringBuilder hintTextBuilder = new StringBuilder();

        if (context.shouldDisplayPronunciation() && type.shouldDisplayPronunciationForAnswerComponent(answer)) {
            hintTextBuilder.append(String.format(
                    getString(R.string.question_activity_hint_pronunciation), answer.getPronunciation()));
        }

        if (context.shouldDisplayExample() && type.shouldDisplayExampleForAnswerComponent(answer)) {
            if (hintTextBuilder.length() > 0) {
                hintTextBuilder.append("<br>");
            }

            hintTextBuilder.append(String.format(
                    getString(R.string.question_activity_hint_example), answer.getExample()));
        }

        hint.setText(HtmlCompat.fromHtml(hintTextBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_short_answer, container, false);

        answer = view.findViewById(R.id.answer);
        hint = view.findViewById(R.id.hint);

        updateHintText();

        return view;
    }

    @Override
    public void restoreQuestion(QuestionContext context, Question question) {
        this.context = context;
        this.question = question;
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

        if (type.getType() == QuestionType.Type.WORD_TO_MEANING) {
            for (final Meaning meaning : question.getAnswer().getWord().getMeanings()) {
                if (type.getAnswerComponent(meaning).toLowerCase().equals(answer)) return true;
            }
        } else if (type.getType() == QuestionType.Type.MEANING_TO_WORD) {
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