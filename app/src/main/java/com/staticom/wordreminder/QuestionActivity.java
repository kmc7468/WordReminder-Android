package com.staticom.wordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Question;
import com.staticom.wordreminder.core.QuestionContext;
import com.staticom.wordreminder.core.QuestionType;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestionActivity extends AppCompatActivity {

    private QuestionContext context;
    private Question question;
    private List<Meaning> wrongAnswers = new ArrayList<>();

    private TextView message;
    private TextView main;
    private TextView hint;
    private AnswerFragment answerFragment;

    private VocabularyMetadata createVocabularyForWrongAnswers() {
        final LocalDateTime time = LocalDateTime.now();
        final VocabularyMetadata vocabulary = new VocabularyMetadata(
                String.format(
                        getString(R.string.question_activity_vocabulary_for_wrong_answers),
                        context.getVocabulary().getName(), time.toString()),
                getFilesDir().toPath().resolve(UUID.randomUUID().toString() + ".kv"), time);
        final Vocabulary wrongAnswers = new Vocabulary();

        for (final Word word : context.getVocabulary().getVocabulary().getWords()) {
            for (final Meaning meaning : word.getMeanings()) {
                if (this.wrongAnswers.contains(meaning)) {
                    wrongAnswers.addWordRef(word);

                    break;
                }
            }
        }

        vocabulary.setVocabulary(wrongAnswers);

        try {
            vocabulary.saveVocabulary();

            Toast.makeText(getApplicationContext(),
                    R.string.question_activity_success_save_vocabulary_for_wrong_answers, Toast.LENGTH_SHORT).show();

            return vocabulary;
        } catch (final Exception e) {
            Toast.makeText(getApplicationContext(),
                    R.string.question_activity_error_save_vocabulary_for_wrong_answers, Toast.LENGTH_LONG).show();

            e.printStackTrace();
            return null;
        }
    }

    private void createVocabularyForWrongAnswersAndFinish() {
        if (!wrongAnswers.isEmpty()) {
            final VocabularyMetadata vocabulary = createVocabularyForWrongAnswers();
            if (createVocabularyForWrongAnswers() == null) return;

            final Intent intent = new Intent();

            intent.putExtra("vocabulary", vocabulary.serialize());

            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    private void createContext() {
        final Intent intent = getIntent();
        final VocabularyMetadata vocabulary =
                VocabularyMetadata.deserialize(intent.getSerializableExtra("vocabulary"));

        context = new QuestionContext(vocabulary);

        context.setDisplayPronunciation(intent.getBooleanExtra("displayPronunciation", false));
        context.setDisplayExample(intent.getBooleanExtra("displayExample", false));

        if (intent.getBooleanExtra("wordToMeaning", false)) {
            context.addUsableType(QuestionType.WordToMeaning.multipleChoice());
        }

        if (intent.getBooleanExtra("wordToMeaningSA", false)) {
            context.addUsableType(QuestionType.WordToMeaning.shortAnswer());
        }

        if (intent.getBooleanExtra("meaningToWord", false)) {
            context.addUsableType(QuestionType.MeaningToWord.multipleChoice());
        }

        if (intent.getBooleanExtra("meaningToWordSA", false)) {
            context.addUsableType(QuestionType.MeaningToWord.shortAnswer());
        }
    }

    private boolean createQuestion() {
        try {
            question = context.createQuestion();

            return true;
        } catch (final Exception e) {
            Toast.makeText(getApplicationContext(),
                    R.string.question_activity_error_create_question, Toast.LENGTH_LONG).show();

            e.printStackTrace();
            return false;
        }
    }

    private void updatedQuestion() {
        final QuestionType type = question.getType();

        message.setText(type.getMessage(this));
        main.setText(type.getMainComponent(question.getAnswer()));

        final StringBuilder hintTextBuilder = new StringBuilder();

        if (context.shouldDisplayPronunciation() && type.shouldDisplayPronunciationForMainComponent(question.getAnswer())) {
            hintTextBuilder.append(String.format(
                    getString(R.string.question_activity_hint_pronunciation), question.getAnswer().getPronunciation()));
        }

        if (context.shouldDisplayExample() && type.shouldDisplayExampleForMainComponent(question.getAnswer())) {
            if (hintTextBuilder.length() > 0) {
                hintTextBuilder.append("<br>");
            }

            hintTextBuilder.append(String.format(
                    getString(R.string.question_activity_hint_example), question.getAnswer().getExample()));
        }

        hint.setText(HtmlCompat.fromHtml(hintTextBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (question.getType().getAnswerType() == QuestionType.AnswerType.MULTIPLE_CHOICE) {
            answerFragment = new MultipleChoiceFragment(context, question);
        } else {
            answerFragment = new ShortAnswerFragment(context, question);
        }

        final FragmentManager manager = getSupportFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();

        transaction.replace(R.id.answerFragmentContainer, (Fragment)answerFragment);
        transaction.commit();
    }

    private boolean createQuestionAndUpdate() {
        if (createQuestion()) {
            updatedQuestion();

            return true;
        } else return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        setTitle(R.string.question_activity_title);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                createVocabularyForWrongAnswersAndFinish();
            }
        });

        createContext();

        message = findViewById(R.id.message);
        main = findViewById(R.id.main);
        hint = findViewById(R.id.hint);

        if (!createQuestionAndUpdate()) {
            finish();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        question = (Question)savedInstanceState.getSerializable("question");
        wrongAnswers = (List<Meaning>)savedInstanceState.getSerializable("wrongAnswers");

        updatedQuestion();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putSerializable("question", question);
        savedInstanceState.putSerializable("wrongAnswers", (Serializable)wrongAnswers);
    }

    public void onStopClick(View view) {
        createVocabularyForWrongAnswersAndFinish();
    }

    public void onSkipClick(View view) {
        wrongAnswers.add(question.getAnswer());

        createQuestionAndUpdate();
    }

    public void onSummitClick(View view) {
        if (answerFragment.isEmptyAnswer()) {
            Toast.makeText(getApplicationContext(),
                    question.getType().getEmptyAnswerMessage(this), Toast.LENGTH_SHORT).show();

            return;
        } else if (!answerFragment.isCorrectAnswer()) {
            wrongAnswers.add(question.getAnswer());

            Toast.makeText(getApplicationContext(),
                    R.string.question_activity_error_wrong_answer, Toast.LENGTH_SHORT).show();

            return;
        }

        createQuestionAndUpdate();
    }
}