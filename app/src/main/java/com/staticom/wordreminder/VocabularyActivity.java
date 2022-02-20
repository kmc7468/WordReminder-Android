package com.staticom.wordreminder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
import com.staticom.wordreminder.utility.CustomDialog;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

public class VocabularyActivity extends AppCompatActivity {

    private Menu menu;
    private MenuItem save;
    private boolean isEdited = false, isSaved = false;

    private RecyclerView words;
    private VocabularyMetadata originalVocabulary, displayedVocabulary;
    private WordsAdapter wordsAdapter;
    private Word selectedWord;

    private RecyclerView meanings;
    private MeaningsAdapter meaningsAdapter;
    private Meaning selectedMeaning;

    private boolean save() {
        try {
            originalVocabulary.saveVocabulary();

            setTitle(R.string.vocabulary_activity_title);

            save.setVisible(false);
            isEdited = false;
            isSaved = true;

            Toast.makeText(getApplicationContext(),
                    R.string.vocabulary_activity_success_save_vocabulary, Toast.LENGTH_SHORT).show();

            return true;
        } catch (final Exception e) {
            Toast.makeText(getApplicationContext(),
                    R.string.vocabulary_activity_error_save_vocabulary, Toast.LENGTH_LONG).show();

            e.printStackTrace();
            return false;
        }
    }

    private void setResultAndFinish() {
        if (isSaved) {
            if (isEdited) {
                try {
                    originalVocabulary.loadVocabulary();
                } catch (final Exception e) {
                    Toast.makeText(getApplicationContext(),
                            R.string.vocabulary_activity_error_restore_vocabulary, Toast.LENGTH_LONG).show();

                    e.printStackTrace();
                    return;
                }
            }

            final Intent intent = new Intent();

            intent.putExtra("vocabulary", originalVocabulary.getVocabulary());

            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    private void askSaveAndFinish() {
        final AlertDialog dialog = new AlertDialog(this,
                R.string.vocabulary_activity_warning_not_saved_vocabulary,
                R.string.vocabulary_activity_ask_save_vocabulary);

        dialog.setPositiveButton(R.string.save, false, () -> {
            if (save()) {
                setResultAndFinish();

                dialog.dismiss();
            }
        }).setNegativeButton(R.string.no_save, true, () -> {
            setResultAndFinish();

            dialog.dismiss();
        }).setNeutralButton(R.string.cancel).show();
    }

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

        meaningsAdapter.setWord(word);
        meaningsAdapter.setOnItemSelectedListener((view, index) -> {
            setSelectedMeaning(word.getMeaning(index));
        });

        setSelectedMeaning(null);
    }

    private void setDisplayedVocabulary(VocabularyMetadata vocabulary) {
        displayedVocabulary = vocabulary;

        wordsAdapter.setVocabulary(vocabulary);
        wordsAdapter.setOnItemSelectedListener((view, index) -> {
            setSelectedWord(vocabulary.getVocabulary().getWord(index));
        });

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isEdited) {
                    askSaveAndFinish();
                } else {
                    setResultAndFinish();
                }
            }
        });

        originalVocabulary = VocabularyMetadata.deserialize(getIntent().getSerializableExtra("vocabulary"));

        words = findViewById(R.id.words);
        wordsAdapter = new WordsAdapter(null);

        words.setLayoutManager(new LinearLayoutManager(this));
        words.setAdapter(wordsAdapter);
        wordsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(words, findViewById(R.id.emptyWordsText)));

        meanings = findViewById(R.id.meanings);
        meaningsAdapter = new MeaningsAdapter(null);

        meanings.setLayoutManager(new LinearLayoutManager(this));
        meanings.setAdapter(meaningsAdapter);
        meaningsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(meanings, findViewById(R.id.emptyMeaningsText)));

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

        isEdited = savedInstanceState.getBoolean("isEdited");
        isSaved = savedInstanceState.getBoolean("isSaved");

        final boolean isSearched = savedInstanceState.getBoolean("isSearched");
        if (isSearched) {
            final String searchQuery = savedInstanceState.getString("searchQuery");
            final Vocabulary searchResult = (Vocabulary)savedInstanceState.getSerializable("searchResult");
            final Vocabulary vocabulary = new Vocabulary();

            for (final Word word : searchResult.getWords()) {
                vocabulary.addWordRef(originalVocabulary.getVocabulary().findWord(word.getWord()));
            }

            setDisplayedVocabulary(new VocabularyMetadata(searchQuery, vocabulary));
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

        savedInstanceState.putBoolean("isEdited", isEdited);
        savedInstanceState.putBoolean("isSaved", isSaved);

        final boolean isSearched = displayedVocabulary != originalVocabulary;

        savedInstanceState.putBoolean("isSearched", isSearched);

        if (isSearched) {
            savedInstanceState.putString("searchQuery", displayedVocabulary.getName());
            savedInstanceState.putSerializable("searchResult", displayedVocabulary.getVocabulary());
        }

        savedInstanceState.putInt("selectedWord", wordsAdapter.getSelectedIndex());
        savedInstanceState.putInt("selectedMeaning", meaningsAdapter.getSelectedIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vocabulary_activity, menu);

        this.menu = menu;
        save = menu.findItem(R.id.save);

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

    private void edited() {
        setTitle(R.string.vocabulary_activity_title_edited);

        save.setVisible(true);
        isEdited = true;
    }

    private void deleteWord(boolean fromDeleteMeaning) {
        final AlertDialog dialog = new AlertDialog(this,
                fromDeleteMeaning ? R.string.vocabulary_activity_delete_meaning : R.string.vocabulary_activity_delete_word,
                fromDeleteMeaning ? R.string.vocabulary_activity_ask_delete_last_meaning : R.string.vocabulary_activity_ask_delete_word);

        dialog.setPositiveButton(R.string.delete, true, () -> {
            final int selectedIndex = wordsAdapter.getSelectedIndex();

            displayedVocabulary.getVocabulary().removeWord(selectedWord);
            wordsAdapter.notifyItemRemoved(selectedIndex);
            wordsAdapter.setSelectedIndex(-1);

            if (displayedVocabulary != originalVocabulary) {
                originalVocabulary.getVocabulary().removeWord(selectedWord);
            }

            meaningsAdapter.setSelectedIndex(-1);

            if (wordsAdapter.getItemCount() > selectedIndex) {
                wordsAdapter.setSelectedIndex(selectedIndex);
            } else if (wordsAdapter.getItemCount() > 0) {
                wordsAdapter.setSelectedIndex(wordsAdapter.getItemCount() - 1);
            } else {
                setSelectedWord(null);
            }

            edited();
        }).setNegativeButton(R.string.cancel).show();
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
            final int selectedIndex = meaningsAdapter.getSelectedIndex();

            selectedWord.removeMeaning(selectedMeaning);
            meaningsAdapter.notifyItemRemoved(selectedIndex);
            meaningsAdapter.setSelectedIndex(-1);

            if (meaningsAdapter.getItemCount() > selectedIndex) {
                meaningsAdapter.setSelectedIndex(selectedIndex);
            } else if (meaningsAdapter.getItemCount() > 0) {
                meaningsAdapter.setSelectedIndex(meaningsAdapter.getItemCount() - 1);
            } else {
                setSelectedMeaning(null);
            }

            edited();
        }).setNegativeButton(R.string.cancel).show();
    }

    private void replaceWord() {
        final AlertDialog dialog = new AlertDialog(this,
                R.string.vocabulary_activity_replace_word,
                R.string.vocabulary_activity_require_word);

        dialog.addEdit(selectedWord.getWord());
        dialog.setPositiveButton(R.string.change, false, () -> {
            final String word = dialog.getEditText().trim();
            if (word.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        R.string.vocabulary_activity_require_word, Toast.LENGTH_SHORT).show();

                return;
            } else if (originalVocabulary.getVocabulary().containsWord(word) && !selectedWord.getWord().equals(word)) {
                Toast.makeText(getApplicationContext(),
                        R.string.vocabulary_activity_error_duplicated_word, Toast.LENGTH_SHORT).show();

                return;
            }

            selectedWord.setWord(word);
            wordsAdapter.notifyItemChanged(wordsAdapter.getSelectedIndex());

            updateCount();
            edited();

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel).show();
    }

    private void replaceMeaning() {
        final AlertDialog dialog = new AlertDialog(this,
                R.string.vocabulary_activity_replace_meaning,
                R.string.vocabulary_activity_require_meaning);

        dialog.addEdit(selectedMeaning.getMeaning());
        dialog.setPositiveButton(R.string.change, false, () -> {
            final String meaning = dialog.getEditText().trim();
            if (meaning.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        R.string.vocabulary_activity_require_meaning, Toast.LENGTH_SHORT).show();

                return;
            } else if (selectedWord.containsMeaning(meaning) && !selectedMeaning.getMeaning().equals(meaning)) {
                Toast.makeText(getApplicationContext(),
                        R.string.vocabulary_activity_error_duplicated_meaning, Toast.LENGTH_SHORT).show();

                return;
            }

            selectedMeaning.setMeaning(meaning);
            meaningsAdapter.notifyItemChanged(meaningsAdapter.getSelectedIndex());

            edited();

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel).show();
    }

    private void setPronunciation() {
        final AlertDialog dialog = new AlertDialog(this,
                R.string.vocabulary_activity_set_pronunciation,
                R.string.vocabulary_activity_require_pronunciation);

        dialog.addEdit(selectedMeaning.getPronunciation());
        dialog.setPositiveButton(R.string.set, false, () -> {
            final String pronunciation = dialog.getEditText().trim();
            if (pronunciation.equals(selectedWord.getWord())) {
                Toast.makeText(getApplicationContext(),
                        R.string.vocabulary_activity_warning_pronunciation_equals_word, Toast.LENGTH_SHORT).show();
            }

            selectedMeaning.setPronunciation(pronunciation);
            meaningsAdapter.notifyItemChanged(meaningsAdapter.getSelectedIndex());

            edited();

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel).show();
    }

    private void setExample() {
        final AlertDialog dialog = new AlertDialog(this,
                R.string.vocabulary_activity_set_example,
                R.string.vocabulary_activity_require_example);

        dialog.addEdit(selectedMeaning.getExample());
        dialog.setPositiveButton(R.string.set, false, () -> {
            final String example = dialog.getEditText().trim();

            selectedMeaning.setExample(example);
            meaningsAdapter.notifyItemChanged(meaningsAdapter.getSelectedIndex());

            edited();

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            save();

            return true;
        } else if (item.getItemId() == R.id.deleteWordOrMeaning) {
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
        } else if (item.getItemId() == R.id.replaceMeaning) {
            replaceMeaning();

            return true;
        } else if (item.getItemId() == R.id.setPronunciation) {
            setPronunciation();

            return true;
        } else if (item.getItemId() == R.id.setExample) {
            setExample();

            return true;
        } else if (item.getItemId() == R.id.deleteMeaning) {
            deleteMeaning();

            return true;
        } else return super.onOptionsItemSelected(item);
    }

    public void onAddClick(View view) {
        final CustomDialog dialog = new CustomDialog(this, R.layout.dialog_add_meaning);

        final TextView word = dialog.findViewById(R.id.word);
        final TextView meaning = dialog.findViewById(R.id.meaning);
        final TextView pronunciation = dialog.findViewById(R.id.pronunciation);
        final TextView example = dialog.findViewById(R.id.example);

        if (selectedWord != null) {
            word.setText(selectedWord.getWord());

            meaning.requestFocus();
        } else {
            word.requestFocus();
        }

        final Button reset = dialog.findViewById(R.id.reset);
        final Button cancel = dialog.findViewById(R.id.cancel);
        final Button add = dialog.findViewById(R.id.add);

        reset.setOnClickListener(v -> {
            word.setText("");
            meaning.setText("");
            pronunciation.setText("");
            example.setText("");

            word.requestFocus();
        });
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        add.setOnClickListener(v -> {
            final String wordStr = word.getText().toString().trim();
            final String meaningStr = meaning.getText().toString().trim();

            final int targetWordIndex = originalVocabulary.getVocabulary().indexOfWord(wordStr);
            Word targetWord;

            if (wordStr.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        R.string.vocabulary_activity_error_empty_word, Toast.LENGTH_SHORT).show();

                return;
            } else if (meaningStr.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        R.string.vocabulary_activity_error_empty_meaning, Toast.LENGTH_SHORT).show();

                return;
            } else if (targetWordIndex != -1) {
                targetWord = originalVocabulary.getVocabulary().getWord(targetWordIndex);
                if (targetWord.containsMeaning(meaningStr)) {
                    Toast.makeText(getApplicationContext(),
                            R.string.vocabulary_activity_error_duplicated_meaning, Toast.LENGTH_SHORT).show();

                    return;
                }
            } else {
                targetWord = new Word(wordStr);

                originalVocabulary.getVocabulary().addWord(targetWord);
            }

            final boolean displayed = displayedVocabulary.getVocabulary().containsWord(targetWord);
            if (!displayed) {
                displayedVocabulary.getVocabulary().addWord(targetWord);
            }

            if (!displayed || targetWordIndex == -1) {
                wordsAdapter.notifyItemInserted(wordsAdapter.getItemCount());
                wordsAdapter.setSelectedIndex(wordsAdapter.getItemCount() - 1);
            }

            targetWord.addMeaning(new Meaning(meaningStr,
                    pronunciation.getText().toString().trim(),
                    example.getText().toString().trim()));

            meaningsAdapter.notifyItemInserted(meaningsAdapter.getItemCount());
            meaningsAdapter.setSelectedIndex(meaningsAdapter.getItemCount() - 1);

            updateCount();
            edited();

            dialog.dismiss();
        });

        dialog.show();
    }
}