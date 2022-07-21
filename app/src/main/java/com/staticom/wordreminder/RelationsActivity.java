package com.staticom.wordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.staticom.wordreminder.adapter.RelationsAdapter;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.Word;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

public class RelationsActivity extends AppCompatActivity {

    private Menu menu;
    private boolean isEdited = false;

    private Vocabulary vocabulary;
    private Word selectedWord;
    private RelationsAdapter relationsAdapter;
    private Pair<Word, String> selectedRelation;

    private void setResultAndFinish() {
        if (isEdited) {
            final Intent intent = new Intent();

            intent.putExtra("vocabulary", vocabulary);

            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relations);
        setTitle(R.string.relations_activity_title);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResultAndFinish();
            }
        });

        vocabulary = (Vocabulary)getIntent().getSerializableExtra("vocabulary");
        selectedWord = vocabulary.getWord(getIntent().getIntExtra("selectedWord", -1));

        final TextView relationsText = findViewById(R.id.relationsText);

        relationsText.setText(HtmlCompat.fromHtml(
                String.format(getString(R.string.relations_activity_relations),
                        selectedWord.getWord(),
                        selectedWord.getRelations().size()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        relationsAdapter = new RelationsAdapter(selectedWord);
        relationsAdapter.setOnItemSelectedListener((view, index) -> {
            selectedRelation = selectedWord.getRelation(index);

            if (menu != null) {
                menu.setGroupVisible(R.id.relationEditMenus, true);
            }
        });
        relationsAdapter.setOnSearchButtonClickListener(index -> {
            /*final Intent intent = new Intent(this, VocabularyViewerActivity.class);

            intent.putExtra("title", String.format(
                    getString(R.string.tag_manager_activity_words_and_meanings_with_tag),
                    selectedTag.getTag()));

            intent.putExtra("wordsTextFormat", getString(R.string.tag_manager_activity_words_with_tag));
            intent.putExtra("defaultMeaningsText", getString(R.string.tag_manager_activity_meanings_with_tag_empty));
            intent.putExtra("meaningsTextFormat", getString(R.string.tag_manager_activity_meanings_with_tag));

            final VocabularyMetadata vocabulary = new VocabularyMetadata(selectedTag.getTag(), null, null);

            vocabulary.setVocabulary(selectedTag.makeVocabulary());

            intent.putExtra("vocabulary", vocabulary.serialize());

            startActivity(intent);*/
            // TODO
        });

        final RecyclerView relations = findViewById(R.id.relations);

        relations.setLayoutManager(new LinearLayoutManager(this));
        relations.setAdapter(relationsAdapter);
        relationsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(relations, findViewById(R.id.emptyRelationsText)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_relations_activity, menu);

        this.menu = menu;

        if (selectedRelation != null) {
            menu.setGroupVisible(R.id.relationEditMenus, true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResultAndFinish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddClick(View view) {

    }
}