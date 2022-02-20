package com.staticom.wordreminder;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.staticom.wordreminder.adapter.MeaningsAdapter;
import com.staticom.wordreminder.adapter.WordsAdapter;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;
import com.staticom.wordreminder.utility.AlertDialog;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

public class VocabularyActivity extends AppCompatActivity {

    private Menu menu;

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

    private void updateMenusVisibility() {
        if (menu != null) {
            menu.setGroupVisible(R.id.wordEditMenus, selectedWord != null);
            menu.setGroupVisible(R.id.meaningEditMenus, selectedMeaning != null);
        }
    }

    private void setSelectedMeaning(Meaning meaning) {
        selectedMeaning = meaning;

        updateMenusVisibility();
        updateCount();
    }

    private void setSelectedWord(Word word) {
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

        setSelectedMeaning(null);
    }

    private void setDisplayedVocabulary(VocabularyMetadata vocabulary) {
        displayedVocabulary = vocabulary;
        wordsAdapter = new WordsAdapter(vocabulary);

        wordsAdapter.setOnItemSelectedListener((view, index) -> {
            setSelectedWord(vocabulary.getVocabulary().getWord(index));
        });

        words.setAdapter(wordsAdapter);

        wordsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(words, findViewById(R.id.emptyWordsText)));

        if (selectedWord != null && vocabulary.getVocabulary().getWords().contains(selectedWord)) {
            final int selectedWordNewIndex = vocabulary.getVocabulary().getWords().indexOf(selectedWord);
            final int selectedMeaningIndex = meaningsAdapter.getSelectedIndex();

            wordsAdapter.setSelectedIndex(selectedWordNewIndex);
            meaningsAdapter.setSelectedIndex(selectedMeaningIndex);
        } else {
            setSelectedWord(null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);
        setTitle(R.string.vocabulary_activity_title);

        originalVocabulary = VocabularyMetadata.deserialize(getIntent().getSerializableExtra("vocabulary"));

        words = findViewById(R.id.words);
        meanings = findViewById(R.id.meanings);

        words.setLayoutManager(new LinearLayoutManager(this));
        meanings.setLayoutManager(new LinearLayoutManager(this));

        setDisplayedVocabulary(originalVocabulary);
    }

    private void searchWord(String query) {
        final String queryLowerCase = query.toLowerCase();
        final Vocabulary searchResult = new Vocabulary();

        for (final Word word : displayedVocabulary.getVocabulary().getWords()) {
            if (word.getWord().toLowerCase().contains(queryLowerCase)) {
                searchResult.addWordRef(word);

                continue;
            }

            for (final Meaning meaning : word.getMeanings()) {
                if (meaning.getMeaning().toLowerCase().contains(queryLowerCase) ||
                        meaning.getPronunciation().toLowerCase().contains(queryLowerCase) ||
                        meaning.getExample().toLowerCase().contains(queryLowerCase)) {
                    searchResult.addWordRef(word);

                    break;
                }
            }
        }

        setDisplayedVocabulary(new VocabularyMetadata(query, searchResult));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final boolean isSearched = savedInstanceState.getBoolean("isSearched");
        if (isSearched) {
            final String searchQuery = savedInstanceState.getString("searchQuery");

            searchWord(searchQuery);
        }

        final int selectedWord = savedInstanceState.getInt("selectedWord");
        if (selectedWord != -1) {
            wordsAdapter.setSelectedIndex(selectedWord);
        }

        final int meaningIndex = savedInstanceState.getInt("selectedMeaning");
        if (meaningIndex != -1) {
            meaningsAdapter.setSelectedIndex(meaningIndex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        final boolean isSearched = displayedVocabulary != originalVocabulary;

        savedInstanceState.putBoolean("isSearched", isSearched);

        if (isSearched) {
            savedInstanceState.putString("searchQuery", displayedVocabulary.getName());
        }

        savedInstanceState.putInt("selectedWord", wordsAdapter.getSelectedIndex());
        savedInstanceState.putInt("selectedMeaning",
                meaningsAdapter != null ? meaningsAdapter.getSelectedIndex() : -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vocabulary_activity, menu);

        this.menu = menu;

        updateMenusVisibility();

        final SearchView searchWord = (SearchView)menu.findItem(R.id.searchWord).getActionView();

        searchWord.setQueryHint(getString(R.string.vocabulary_activity_search_hint));
        searchWord.setMaxWidth(Integer.MAX_VALUE);
        searchWord.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchWord(query.trim());

                final InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                manager.hideSoftInputFromWindow(searchWord.getWindowToken(), 0);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchWord.setOnCloseListener(() -> {
            setDisplayedVocabulary(originalVocabulary);

            return true;
        });
        searchWord.setIconified(false);

        return true;
    }

    private void deleteWord(boolean fromDeleteMeaning) {
        final AlertDialog dialog = new AlertDialog(this,
                fromDeleteMeaning ? R.string.vocabulary_activity_delete_meaning : R.string.vocabulary_activity_delete_word,
                fromDeleteMeaning ? R.string.vocabulary_activity_ask_delete_last_meaning : R.string.vocabulary_activity_ask_delete_word);

        dialog.setPositiveButton(R.string.delete, true, () -> {
            displayedVocabulary.getVocabulary().removeWord(selectedWord);
            wordsAdapter.notifyItemRemoved(wordsAdapter.getSelectedIndex());
            wordsAdapter.setSelectedIndex(-1);

            if (displayedVocabulary != originalVocabulary) {
                originalVocabulary.getVocabulary().removeWord(selectedWord);
            }

            setSelectedWord(null);
        }).setNegativeButton(R.string.cancel).show();
    }

    private void replaceWord() {
        // TODO
    }

    private void deleteMeaning() {
        if (selectedWord.getMeanings().size() == 1) {
            deleteWord(true);

            return;
        }

        final AlertDialog dialog = new AlertDialog(this,
                R.string.vocabulary_activity_delete_meaning,
                R.string.vocabulary_activity_ask_delete_meaning);

        dialog.setPositiveButton(R.string.delete, true, () -> {
            selectedWord.removeMeaning(selectedMeaning);
            meaningsAdapter.notifyItemRemoved(meaningsAdapter.getSelectedIndex());
            meaningsAdapter.setSelectedIndex(-1);

            setSelectedMeaning(null);
        }).setNegativeButton(R.string.cancel).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.deleteWordOrMeaning) {
            if (selectedMeaning == null) {
                deleteWord(false);
            } else {
                deleteMeaning();
            }

            return true;
        } else if (item.getItemId() == R.id.replaceWord) {
            replaceWord();

            return true;
        } else if (item.getItemId() == R.id.deleteWord) {
            deleteWord(false);

            return true;
        } else if (item.getItemId() == R.id.deleteMeaning) {
            deleteMeaning();

            return true;
        }

        // TODO

        return super.onOptionsItemSelected(item);
    }
}