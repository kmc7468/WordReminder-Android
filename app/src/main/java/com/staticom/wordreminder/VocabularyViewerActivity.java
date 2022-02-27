package com.staticom.wordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.staticom.wordreminder.core.VocabularyMetadata;

public class VocabularyViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Intent intent = getIntent();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_viewer);
        setTitle(intent.getStringExtra("title"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final FragmentManager manager = getSupportFragmentManager();
        final Fragment fragment = manager.findFragmentByTag("vocabularyFragment");

        if (fragment == null) {
            final VocabularyFragment vocabularyFragment = new VocabularyFragment();

            vocabularyFragment.setWordsTextFormat(intent.getStringExtra("wordsTextFormat"));
            vocabularyFragment.setDefaultMeaningsText(intent.getStringExtra("defaultMeaningsText"));
            vocabularyFragment.setMeaningsTextFormat(intent.getStringExtra("meaningsTextFormat"));
            vocabularyFragment.setVocabulary(
                    VocabularyMetadata.deserialize(intent.getSerializableExtra("vocabulary")));

            final FragmentTransaction transaction = manager.beginTransaction();

            transaction.add(R.id.vocabulary, vocabularyFragment, "vocabularyFragment");
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

            return true;
        } else return super.onOptionsItemSelected(item);
    }
}