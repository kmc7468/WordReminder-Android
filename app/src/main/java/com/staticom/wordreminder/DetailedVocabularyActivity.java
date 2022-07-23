package com.staticom.wordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SearchView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.staticom.wordreminder.adapter.DetailedWordsAdapter;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

public class DetailedVocabularyActivity extends AppCompatActivity {

    private VocabularyMetadata vocabulary;
    private boolean isEdited = false;
    private RecyclerView words;
    private DetailedWordsAdapter wordsAdapter;

    private ActivityResultLauncher<Intent> editVocabularyResult;

    private void setResultAndFinish() {
        if (isEdited) {
            final Intent intent = new Intent();

            intent.putExtra("vocabulary", vocabulary.serialize());

            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    private void updateVocabulary(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK) return;

        final Intent intent = result.getData();
        final VocabularyMetadata vocabulary = VocabularyMetadata.deserialize(intent.getSerializableExtra("vocabulary"));

        this.vocabulary.setVocabulary(vocabulary.getVocabulary());
        //this.vocabulary.setTime(vocabulary.getTime()); TODO

        wordsAdapter.setVocabulary(vocabulary.getVocabulary());

        isEdited = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_vocabulary);
        setTitle(R.string.detailed_vocabulary_activity_title);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResultAndFinish();
            }
        });

        vocabulary = VocabularyMetadata.deserialize(getIntent().getSerializableExtra("vocabulary"));

        if (savedInstanceState != null) {
            isEdited = savedInstanceState.getBoolean("isEdited");

            if (isEdited) {
                vocabulary = VocabularyMetadata.deserialize(savedInstanceState.getSerializable("vocabulary"));
            }
        }

        wordsAdapter = new DetailedWordsAdapter(vocabulary.getVocabulary());

        words = findViewById(R.id.words);

        words.setLayoutManager(new LinearLayoutManager(this));
        words.setAdapter(wordsAdapter);
        words.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        wordsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(words, findViewById(R.id.emptyWordsText)));

        editVocabularyResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::updateVocabulary);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        wordsAdapter.setSelectedIndex(savedInstanceState.getInt("selectedWord"));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("isEdited", isEdited);

        if (isEdited) {
            savedInstanceState.putSerializable("vocabulary", vocabulary.serialize());
        }

        savedInstanceState.putInt("selectedWord", wordsAdapter.getSelectedIndex());
    }

    public void setVocabulary(Vocabulary vocabulary) {
        Word selectedWord = null;

        if (wordsAdapter.getSelectedIndex() != -1) {
            selectedWord = wordsAdapter.getSelectedWord();
        }

        wordsAdapter.setVocabulary(vocabulary);

        if (selectedWord != null) {
            if (vocabulary.getWords().contains(selectedWord)) {
                final int selectedWordNewIndex = vocabulary.getWords().indexOf(selectedWord);

                words.scrollToPosition(selectedWordNewIndex);
                wordsAdapter.setSelectedIndex(selectedWordNewIndex);
            } else {
                wordsAdapter.setSelectedIndex(-1);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detailed_vocabulary_activity, menu);

        final SearchView searchWord = (SearchView)menu.findItem(R.id.searchWord).getActionView();

        searchWord.setQueryHint(getString(R.string.detailed_vocabulary_activity_search_hint));
        searchWord.setMaxWidth(Integer.MAX_VALUE);
        searchWord.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setVocabulary(vocabulary.getVocabulary().search(query.trim()));

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchWord.setOnCloseListener(() -> {
            setVocabulary(vocabulary.getVocabulary());

            return true;
        });
        searchWord.setIconified(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResultAndFinish();

            return true;
        }

        if (item.getItemId() == R.id.edit) {
            final Intent intent = new Intent(this, VocabularyActivity.class);

            intent.putExtra("vocabulary", vocabulary.serialize());
            intent.putExtra("selectedWord", wordsAdapter.getSelectedIndex());

            editVocabularyResult.launch(intent);

            return true;
        } else return super.onOptionsItemSelected(item);
    }

    public void onHideWordClick(View view) {
        wordsAdapter.setHideWord(((CheckBox)view).isChecked());
    }

    public void onHideMeaningsClick(View view) {
        wordsAdapter.setHideMeanings(((CheckBox)view).isChecked());
    }

    public void onHideHintsClick(View view) {
        wordsAdapter.setHideHints(((CheckBox)view).isChecked());
    }
}