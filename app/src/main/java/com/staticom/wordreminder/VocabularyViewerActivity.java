package com.staticom.wordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.staticom.wordreminder.core.VocabularyMetadata;

public class VocabularyViewerActivity extends AppCompatActivity {

    private Menu menu;
    private boolean isSelectMode = false;

    private VocabularyMetadata vocabulary;
    private VocabularyFragment vocabularyFragment;

    private void setResultAndFinish() {
        if (isSelectMode) {
            if (vocabularyFragment.getSelectedWordIndex() != -1) {
                final Intent intent = new Intent();

                intent.putExtra("selectedWord", vocabularyFragment.getSelectedWord().getWord());

                setResult(RESULT_OK, intent);
            } else {
                setResult(RESULT_CANCELED);
            }
        }

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Intent intent = getIntent();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_viewer);
        setTitle(intent.getStringExtra("title"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResultAndFinish();
            }
        });

        vocabulary = VocabularyMetadata.deserialize(intent.getSerializableExtra("vocabulary"));

        final FragmentManager manager = getSupportFragmentManager();
        final Fragment vocabularyFragment = manager.findFragmentByTag("vocabularyFragment");

        if (vocabularyFragment != null) {
            this.vocabularyFragment = (VocabularyFragment)vocabularyFragment;
        } else {
            this.vocabularyFragment = new VocabularyFragment();

            this.vocabularyFragment.setWordsTextFormat(intent.getStringExtra("wordsTextFormat"));
            this.vocabularyFragment.setDefaultMeaningsText(intent.getStringExtra("defaultMeaningsText"));
            this.vocabularyFragment.setMeaningsTextFormat(intent.getStringExtra("meaningsTextFormat"));
            this.vocabularyFragment.setVocabulary(vocabulary);

            final FragmentTransaction transaction = manager.beginTransaction();

            transaction.add(R.id.vocabulary, this.vocabularyFragment, "vocabularyFragment");
            transaction.commit();
        }

        if (intent.getBooleanExtra("isSelectMode", false)) {
            isSelectMode = true;

            this.vocabularyFragment.setOnWordSelectedListener((view, index) -> {
                menu.findItem(R.id.select).setVisible(true);
            });
        }
    }

    private void searchWord(String query) {
        vocabularyFragment.setWordsTextFormat(getString(R.string.vocabulary_viewer_activity_words_search_result));
        vocabularyFragment.setVocabulary(new VocabularyMetadata(query, vocabulary.getVocabulary().search(query)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vocabulary_viewer_activity, menu);

        this.menu = menu;

        final SearchView searchWord = (SearchView)menu.findItem(R.id.searchWord).getActionView();

        searchWord.setQueryHint(getString(R.string.vocabulary_viewer_activity_search_hint));
        searchWord.setMaxWidth(Integer.MAX_VALUE);
        searchWord.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchWord(query.trim());

                if (isSelectMode && vocabularyFragment.getSelectedWordIndex() == -1) {
                    menu.findItem(R.id.select).setVisible(false);
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchWord.setOnCloseListener(() -> {
            vocabularyFragment.setWordsTextFormat(getIntent().getStringExtra("wordsTextFormat"));
            vocabularyFragment.setVocabulary(vocabulary);

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

        if (item.getItemId() == R.id.select) {
            setResultAndFinish();

            return true;
        } else return super.onOptionsItemSelected(item);
    }
}