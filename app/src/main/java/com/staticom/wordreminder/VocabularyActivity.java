package com.staticom.wordreminder;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.staticom.wordreminder.adapter.MeaningsAdapter;
import com.staticom.wordreminder.adapter.WordsAdapter;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

public class VocabularyActivity extends AppCompatActivity {

    private RecyclerView words;
    private VocabularyMetadata originalVocabulary, displayedVocabulary;
    private WordsAdapter wordsAdapter;
    private Word selectedWord;

    private RecyclerView meanings;
    private MeaningsAdapter meaningsAdapter;
    private Meaning selectedMeaning;

    private void updateCount() {
        final TextView wordsText = findViewById(R.id.wordsText);
        final String wordsFormatString = getString(displayedVocabulary == originalVocabulary ?
                R.string.vocabulary_activity_words : R.string.vocabulary_activity_words_search_result);

        wordsText.setText(HtmlCompat.fromHtml(
                String.format(wordsFormatString,
                        displayedVocabulary.getName(),
                        displayedVocabulary.getVocabulary().getWords().size()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        final TextView meaningsText = findViewById(R.id.meaningsText);

        if (selectedWord != null) {
            meaningsText.setText(HtmlCompat.fromHtml(
                    String.format(getString(R.string.vocabulary_activity_meanings),
                            selectedWord.getWord(),
                            selectedWord.getMeanings().size()),
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            meaningsText.setText(HtmlCompat.fromHtml(
                    getString(R.string.vocabulary_activity_meanings_empty), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    private void setSelectedMeaning(Meaning meaning) {
        selectedMeaning = meaning;

        // TODO: MENU

        updateCount();
    }

    private void setSelectedWord(Word word, boolean resetSelectedMeaning) {
        selectedWord = word;

        if (word != null) {
            meaningsAdapter = new MeaningsAdapter(word);

            meaningsAdapter.setOnItemSelectedListener((view, index) -> {
                setSelectedMeaning(word.getMeaning(index));
            });
        } else {
            meaningsAdapter = null;
        }

        meanings.setAdapter(meaningsAdapter);

        if (meaningsAdapter != null) {
            meaningsAdapter.registerAdapterDataObserver(
                    new RecyclerViewEmptyObserver(meanings, findViewById(R.id.emptyMeaningsText)));
        }

        if (resetSelectedMeaning) {
            setSelectedMeaning(null);
        } else {
            updateCount();
        }
    }

    private void setDisplayedVocabulary(VocabularyMetadata vocabulary) {
        displayedVocabulary = vocabulary;
        wordsAdapter = new WordsAdapter(vocabulary);

        wordsAdapter.setOnItemSelectedListener((view, index) -> {
            setSelectedWord(vocabulary.getVocabulary().getWord(index), true);
        });

        words.setAdapter(wordsAdapter);

        wordsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(words, findViewById(R.id.emptyWordsText)));

        if (selectedWord != null && vocabulary.getVocabulary().getWords().contains(selectedWord)) {
            updateCount();
        } else {
            setSelectedWord(null, true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);

        originalVocabulary = MainActivity.SelectedVocabularyForVocabularyActivity;

        words = findViewById(R.id.words);
        meanings = findViewById(R.id.meanings);

        words.setLayoutManager(new LinearLayoutManager(this));
        meanings.setLayoutManager(new LinearLayoutManager(this));

        setDisplayedVocabulary(originalVocabulary);
    }
}