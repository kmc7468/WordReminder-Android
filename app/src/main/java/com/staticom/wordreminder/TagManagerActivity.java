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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.staticom.wordreminder.adapter.TagsAdapter;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Tag;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;
import com.staticom.wordreminder.utility.AlertDialog;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

public class TagManagerActivity extends AppCompatActivity {

    private Menu menu;
    private boolean isEdited = false;

    private VocabularyMetadata vocabulary;
    private TagsAdapter tagsAdapter;
    private Tag selectedTag;

    private void setResultAndFinish() {
        if (isEdited) {
            final Intent intent = new Intent();

            intent.putExtra("vocabulary", vocabulary.getVocabulary());

            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_manager);
        setTitle(R.string.tag_manager_activity_title);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResultAndFinish();
            }
        });

        vocabulary = VocabularyMetadata.deserialize(getIntent().getSerializableExtra("vocabulary"));

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("isEdited")) {
                vocabulary = VocabularyMetadata.deserialize(savedInstanceState.getSerializable("vocabulary"));

                edited();
            }
        }

        final TextView tagsText = findViewById(R.id.tagsText);

        tagsText.setText(HtmlCompat.fromHtml(
                String.format(getString(R.string.tag_manager_activity_tags),
                        vocabulary.getName(),
                        vocabulary.getVocabulary().getTags().size()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

        tagsAdapter = new TagsAdapter(vocabulary.getVocabulary());
        tagsAdapter.setOnItemSelectedListener((view, index) -> {
            selectedTag = vocabulary.getVocabulary().getTag(index);

            if (menu != null) {
                menu.setGroupVisible(R.id.tagEditMenus, true);
            }
        });
        tagsAdapter.setOnListButtonClickListener(index -> {
            final Intent intent = new Intent(this, VocabularyViewerActivity.class);

            intent.putExtra("title", String.format(
                    getString(R.string.tag_manager_activity_words_and_meanings_with_tag),
                    selectedTag.getTag()));

            intent.putExtra("wordsTextFormat", getString(R.string.tag_manager_activity_words_with_tag));
            intent.putExtra("defaultMeaningsText", getString(R.string.tag_manager_activity_meanings_with_tag_empty));
            intent.putExtra("meaningsTextFormat", getString(R.string.tag_manager_activity_meanings_with_tag));

            final VocabularyMetadata vocabulary = new VocabularyMetadata(selectedTag.getTag(), null, null);

            vocabulary.setVocabulary(selectedTag.makeVocabulary());

            intent.putExtra("vocabulary", vocabulary.serialize());

            startActivity(intent);
        });

        final RecyclerView tags = findViewById(R.id.tags);

        tags.setLayoutManager(new LinearLayoutManager(this));
        tags.setAdapter(tagsAdapter);
        tagsAdapter.registerAdapterDataObserver(
                new RecyclerViewEmptyObserver(tags, findViewById(R.id.emptyTagsText)));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        tagsAdapter.setSelectedIndex(savedInstanceState.getInt("selectedTag"));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("isEdited", isEdited);

        if (isEdited) {
            savedInstanceState.putSerializable("vocabulary", vocabulary.serialize());
        }

        savedInstanceState.putInt("selectedTag", tagsAdapter.getSelectedIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tag_manager_activity, menu);

        this.menu = menu;

        if (selectedTag != null) {
            menu.setGroupVisible(R.id.tagEditMenus, true);
        }

        return true;
    }

    private void edited() {
        setTitle(R.string.tag_manager_activity_title_edited);

        isEdited = true;
    }

    private void deleteTag() {
        final AlertDialog dialog = new AlertDialog(this,
                R.string.tag_manager_activity_delete_tag,
                R.string.tag_manager_activity_ask_delete_tag);

        dialog.setPositiveButton(R.string.delete, true, () -> {
            final int selectedIndex = tagsAdapter.getSelectedIndex();

            for (final Word word : vocabulary.getVocabulary().getWords()) {
                for (final Meaning meaning : word.getMeanings()) {
                    meaning.removeTag(selectedTag);
                }
            }

            vocabulary.getVocabulary().removeTag(selectedTag);
            tagsAdapter.notifyItemRemoved(selectedIndex);
            tagsAdapter.setSelectedIndex(-1);

            if (tagsAdapter.getItemCount() > selectedIndex) {
                tagsAdapter.setSelectedIndex(selectedIndex);
            } else if (tagsAdapter.getItemCount() > 0) {
                tagsAdapter.setSelectedIndex(tagsAdapter.getItemCount() - 1);
            } else {
                selectedTag = null;

                menu.setGroupVisible(R.id.tagEditMenus, false);
            }

            edited();
        }).setNegativeButton(R.string.cancel).show();
    }

    private void renameTag() {
        final AlertDialog dialog = new AlertDialog(this,
                R.string.tag_manager_activity_rename_tag,
                R.string.tag_manager_activity_require_tag_name);

        dialog.addEdit(selectedTag.getTag());
        dialog.setPositiveButton(R.string.change, false, () -> {
            final String tag = dialog.getEditText().trim();
            if (tag.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        R.string.tag_manager_activity_error_empty_tag, Toast.LENGTH_SHORT).show();

                return;
            } else if (vocabulary.getVocabulary().containsTag(tag) && !selectedTag.getTag().equals(tag)) {
                Toast.makeText(getApplicationContext(),
                        R.string.tag_manager_activity_error_duplicated_tag, Toast.LENGTH_SHORT).show();

                return;
            }

            selectedTag.setTag(tag);
            tagsAdapter.notifyItemChanged(tagsAdapter.getSelectedIndex());

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
            deleteTag();

            return true;
        } else if (item.getItemId() == R.id.rename) {
            renameTag();

            return true;
        } else return super.onOptionsItemSelected(item);
    }

    public void onAddClick(View view) {
        final EditText name = findViewById(R.id.name);
        final String tagName = name.getText().toString().trim();
        if (tagName.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    R.string.tag_manager_activity_error_empty_tag, Toast.LENGTH_SHORT).show();

            return;
        } else if (vocabulary.getVocabulary().containsTag(tagName)) {
            Toast.makeText(getApplicationContext(),
                    R.string.tag_manager_activity_error_duplicated_tag, Toast.LENGTH_SHORT).show();

            return;
        }

        name.setText("");

        vocabulary.getVocabulary().addTag(new Tag(vocabulary.getVocabulary(), tagName));
        tagsAdapter.notifyItemInserted(tagsAdapter.getItemCount());
        tagsAdapter.setSelectedIndex(tagsAdapter.getItemCount() - 1);

        edited();
    }
}