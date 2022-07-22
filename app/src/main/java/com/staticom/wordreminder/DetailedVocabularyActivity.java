package com.staticom.wordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.staticom.wordreminder.adapter.DetailedWordsAdapter;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

public class DetailedVocabularyActivity extends AppCompatActivity {

    private VocabularyMetadata vocabulary;
    private boolean isEdited = false;
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

        wordsAdapter.notifyDataSetChanged();

        isEdited = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_vocabulary);
        setTitle(R.string.detailed_vocabulary_activity_title);

        //Toast.makeText(getApplicationContext(), "onCreated호출" + LocalDateTime.now().toString(), Toast.LENGTH_SHORT).show();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResultAndFinish();
            }
        });

        vocabulary = VocabularyMetadata.deserialize(getIntent().getSerializableExtra("vocabulary"));
        //Toast.makeText(getApplicationContext(), "단어장저장완료" + LocalDateTime.now().toString(), Toast.LENGTH_SHORT).show();

        wordsAdapter = new DetailedWordsAdapter(vocabulary);

        final RecyclerView words = findViewById(R.id.words);

        words.setLayoutManager(new LinearLayoutManager(this));
        words.setAdapter(wordsAdapter);
        words.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        wordsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(words, findViewById(R.id.emptyWordsText)));

        editVocabularyResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::updateVocabulary);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detailed_vocabulary_activity, menu);

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