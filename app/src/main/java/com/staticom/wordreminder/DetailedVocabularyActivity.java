package com.staticom.wordreminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.staticom.wordreminder.adapter.DetailedWordsAdapter;
import com.staticom.wordreminder.adapter.VocabularyListAdapter;
import com.staticom.wordreminder.core.VocabularyList;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

public class DetailedVocabularyActivity extends AppCompatActivity {

    private VocabularyMetadata vocabulary;
    private DetailedWordsAdapter wordsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_vocabulary);
        setTitle(R.string.detailed_vocabulary_activity_title);

        vocabulary = VocabularyMetadata.deserialize(getIntent().getSerializableExtra("vocabulary"));

        wordsAdapter = new DetailedWordsAdapter(vocabulary);

        final RecyclerView words = findViewById(R.id.words);

        words.setLayoutManager(new LinearLayoutManager(this));
        words.setAdapter(wordsAdapter);

        wordsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(words, findViewById(R.id.emptyWordsText)));
    }
}