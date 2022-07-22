package com.staticom.wordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.staticom.wordreminder.adapter.RelationsAdapter;
import com.staticom.wordreminder.core.Relation;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;
import com.staticom.wordreminder.utility.AlertDialog;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

public class RelationsActivity extends AppCompatActivity {

    private Menu menu;
    private boolean isEdited = false;
    private ActivityResultLauncher<Intent> changeRelatedWordResult;

    private Vocabulary vocabulary;
    private Word selectedWord;

    private TextView relationsText;
    private RelationsAdapter relationsAdapter;
    private Relation selectedRelation;

    private EditText relation;
    private ActivityResultLauncher<Intent> selectWordResult;

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

    private void changeRelatedWord(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK) return;

        final Intent intent = result.getData();
        final Word selectedWord = vocabulary.findWord(intent.getStringExtra("selectedWord"));

        selectedRelation.getWord().removeRelation(this.selectedWord);
        selectedRelation.setWord(selectedWord);
        selectedWord.addRelation(this.selectedWord, selectedRelation.getRelation());

        relationsAdapter.notifyItemChanged(relationsAdapter.getSelectedIndex());

        updateCount();
        edited();
    }

    private void updateCount() {
        relationsText.setText(HtmlCompat.fromHtml(
                String.format(getString(R.string.relations_activity_relations),
                        selectedWord.getWord(),
                        selectedWord.getRelations().size()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    private void edited() {
        setTitle(R.string.relations_activity_title_edited);

        isEdited = true;
    }

    private void addRelation(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK) return;

        final Intent intent = result.getData();
        final Word selectedWord = vocabulary.findWord(intent.getStringExtra("selectedWord"));
        final String relation = this.relation.getText().toString().trim();

        this.selectedWord.addRelation(selectedWord, relation);
        selectedWord.addRelation(this.selectedWord, relation);

        relationsAdapter.notifyItemInserted(this.selectedWord.getRelations().size());
        relationsAdapter.setSelectedIndex(this.selectedWord.getRelations().size() - 1);

        this.relation.setText("");

        updateCount();
        edited();
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

        changeRelatedWordResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::changeRelatedWord);

        vocabulary = (Vocabulary)getIntent().getSerializableExtra("vocabulary");
        selectedWord = vocabulary.getWord(getIntent().getIntExtra("selectedWord", -1));

        relationsText = findViewById(R.id.relationsText);

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

        relation = findViewById(R.id.relation);
        selectWordResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::addRelation);

        updateCount();
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

    private void deleteRelation() {
        final AlertDialog dialog = new AlertDialog(this,
                R.string.relations_activity_delete_relation,
                R.string.relations_activity_ask_delete_relation);

        dialog.setPositiveButton(R.string.delete, true, () -> {
            final int selectedIndex = relationsAdapter.getSelectedIndex();

            selectedWord.removeRelation(selectedIndex);
            selectedRelation.getWord().removeRelation(selectedWord);

            relationsAdapter.notifyItemRemoved(selectedIndex);
            relationsAdapter.setSelectedIndex(-1);

            if (relationsAdapter.getItemCount() > selectedIndex) {
                relationsAdapter.setSelectedIndex(selectedIndex);
            } else if (relationsAdapter.getItemCount() > 0) {
                relationsAdapter.setSelectedIndex(relationsAdapter.getItemCount() - 1);
            } else {
                selectedRelation = null;

                menu.setGroupVisible(R.id.relationEditMenus, false);
            }

            edited();
        }).setNegativeButton(R.string.cancel).show();
    }

    private Intent createIntentToSelectWord(String relation) {
        final Intent intent = new Intent(this, VocabularyViewerActivity.class);

        intent.putExtra("title", String.format(
                getString(R.string.relations_activity_select_word),
                selectedWord.getWord(), relation));
        intent.putExtra("isSelectMode", true);

        intent.putExtra("wordsTextFormat", getString(R.string.relations_activity_selectable_words));
        intent.putExtra("defaultMeaningsText", getString(R.string.relations_activity_meanings_empty));
        intent.putExtra("meaningsTextFormat", getString(R.string.relations_activity_meanings));

        final VocabularyMetadata vocabulary = new VocabularyMetadata("", null, null);
        final Vocabulary selectableVocabulary = new Vocabulary();

        for (Word word : this.vocabulary.getWords()) {
            if (word != selectedWord && !selectedWord.containsRelation(word)) {
                selectableVocabulary.addWordRef(word);
            }
        }

        vocabulary.setVocabulary(selectableVocabulary);

        intent.putExtra("vocabulary", vocabulary.serialize());

        return intent;
    }

    private void renameRelation() {
        final AlertDialog dialog = new AlertDialog(this,
                R.string.relations_activity_rename_relation,
                R.string.relations_activity_require_relation);

        dialog.addEdit(selectedRelation.getRelation());
        dialog.setPositiveButton(R.string.change, false, () -> {
            final String relation = dialog.getEditText().trim();
            if (relation.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        R.string.relations_activity_error_empty_relation, Toast.LENGTH_SHORT).show();

                return;
            }

            selectedRelation.setRelation(relation);
            selectedRelation.getWord().findRelation(selectedWord).setRelation(relation);

            relationsAdapter.notifyItemChanged(relationsAdapter.getSelectedIndex());

            edited();

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResultAndFinish();

            return true;
        }

        if (item.getItemId() == R.id.delete) {
            deleteRelation();

            return true;
        } else if (item.getItemId() == R.id.changeWord) {
            changeRelatedWordResult.launch(createIntentToSelectWord(selectedRelation.getRelation()));

            return true;
        } else if (item.getItemId() == R.id.rename) {
            renameRelation();

            return true;
        } else return super.onOptionsItemSelected(item);
    }

    public void onAddClick(View view) {
        final String relation = this.relation.getText().toString().trim();
        if (relation.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    R.string.relations_activity_error_empty_relation, Toast.LENGTH_SHORT).show();

            return;
        }

        selectWordResult.launch(createIntentToSelectWord(relation));
    }
}