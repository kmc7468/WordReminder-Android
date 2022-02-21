package com.staticom.wordreminder.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class QuestionContext {

    private static final int LOOP_LIMIT = 1000;

    private interface LoopBody<T> {
        boolean runOnce(AtomicReference<T> result);
    }

    private final Random random = new Random();

    private final VocabularyMetadata vocabulary;
    private final List<QuestionType> usableTypes = new ArrayList<>();

    private boolean displayPronunciation = false;
    private boolean displayExample = false;

    public QuestionContext(VocabularyMetadata vocabulary) {
        this.vocabulary = vocabulary;
    }

    public VocabularyMetadata getVocabulary() {
        return vocabulary;
    }

    private QuestionType getRandomUsableType() {
        return usableTypes.get(random.nextInt(usableTypes.size()));
    }

    public void addUsableType(QuestionType type) {
        usableTypes.add(type);
    }

    public boolean shouldDisplayPronunciation() {
        return displayPronunciation;
    }

    public void setDisplayPronunciation(boolean displayPronunciation) {
        this.displayPronunciation = displayPronunciation;
    }

    public boolean shouldDisplayExample() {
        return displayExample;
    }

    public void setDisplayExample(boolean displayExample) {
        this.displayExample = displayExample;
    }

    private <T> T loop(LoopBody<T> body) throws Exception {
        final AtomicReference<T> result = new AtomicReference<>();

        for (int i = 0; i < LOOP_LIMIT; ++i) {
            if (body.runOnce(result)) return result.get();
        }

        throw new Exception();
    }

    private Meaning getRandomMeaning() {
        final Vocabulary vocabulary = this.vocabulary.getVocabulary();
        final Word word = vocabulary.getWord(random.nextInt(vocabulary.getWords().size()));

        return word.getMeaning(random.nextInt(word.getMeanings().size()));
    }

    public Question createQuestion() throws Exception {
        final QuestionType type = getRandomUsableType();
        final Meaning answer = loop(result -> {
            final Meaning meaning = getRandomMeaning();

            if (type.isUsableForAnswer(meaning)) {
                result.set(meaning);

                return true;
            } else return false;
        });

        Meaning[] choices = null;

        if (type.getAnswerType() == QuestionType.AnswerType.MULTIPLE_CHOICE) {
            choices = new Meaning[] { answer, null, null, null, null };

            for (int i = 1; i < 5; ++i) {
                final int finalI = i;
                final Meaning[] finalChoices = choices;

                choices[i] = loop(result -> {
                    final Meaning meaning = getRandomMeaning();
                    if (!type.isUsableForAnswer(meaning)) return false;

                    for (int j = 0; j < finalI; ++j) {
                        if (type.isDuplicatedForAnswer(finalChoices[j], meaning)) return false;
                    }

                    result.set(meaning);

                    return true;
                });
            }

            Collections.shuffle(Arrays.asList(choices));
        }

        return new Question(type, answer, choices);
    }
}