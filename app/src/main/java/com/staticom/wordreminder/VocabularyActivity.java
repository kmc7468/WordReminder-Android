package com.staticom.wordreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.staticom.wordreminder.adapter.CheckableAdapter;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Tag;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;
import com.staticom.wordreminder.utility.AlertDialog;
import com.staticom.wordreminder.utility.CustomDialog;
import com.staticom.wordreminder.utility.TagsSpinner;

import java.util.List;

public class VocabularyActivity extends AppCompatActivity {

    private Menu menu;
    private MenuItem save;
    private boolean isEdited = false, isSaved = false;

    private VocabularyFragment vocabularyFragment;
    private VocabularyMetadata originalVocabulary;

    private ActivityResultLauncher<Intent> tagManagerResult;

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
            }

            dialog.dismiss();
        }).setNegativeButton(R.string.no_save, true, this::setResultAndFinish).setNeutralButton(R.string.cancel).show();
    }

    private void updateMenusVisibility() {
        if (menu != null) {
            menu.setGroupVisible(R.id.wordEditMenus, vocabularyFragment.getSelectedWordIndex() != -1);
            menu.setGroupVisible(R.id.meaningEditMenus, vocabularyFragment.getSelectedMeaningIndex() != -1);
        }
    }

    private void edited() {
        setTitle(R.string.vocabulary_activity_title_edited);

        save.setVisible(true);
        isEdited = true;
    }

    private void updateVocabulary(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK) return;

        final Intent intent = result.getData();
        final Vocabulary newVocabulary = (Vocabulary)intent.getSerializableExtra("vocabulary");

        originalVocabulary.setVocabulary(newVocabulary);

        final VocabularyMetadata displayedVocabulary = vocabularyFragment.getVocabulary();
        if (displayedVocabulary != originalVocabulary) {
            final Vocabulary newDisplayedVocabulary = new Vocabulary();

            for (final Word word : displayedVocabulary.getVocabulary().getWords()) {
                newDisplayedVocabulary.addWordRef(newVocabulary.findWord(word.getWord()));
            }

            displayedVocabulary.setVocabulary(newDisplayedVocabulary);
        }

        vocabularyFragment.notifyVocabularyUpdated();

        if (vocabularyFragment.getSelectedWordIndex() != -1) {
            vocabularyFragment.notifyMeaningsUpdated();
        }

        edited();
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

        final FragmentManager manager = getSupportFragmentManager();
        final Fragment vocabularyFragment = manager.findFragmentByTag("vocabularyFragment");

        if (vocabularyFragment != null) {
            this.vocabularyFragment = (VocabularyFragment)vocabularyFragment;
        } else {
            this.vocabularyFragment = new VocabularyFragment();

            this.vocabularyFragment.setWordsTextFormat(getString(R.string.vocabulary_activity_words));
            this.vocabularyFragment.setDefaultMeaningsText(getString(R.string.vocabulary_activity_meanings_empty));
            this.vocabularyFragment.setMeaningsTextFormat(getString(R.string.vocabulary_activity_meanings));

            final FragmentTransaction transaction = manager.beginTransaction();

            transaction.add(R.id.vocabulary, this.vocabularyFragment, "vocabularyFragment");
            transaction.commit();
        }

        this.vocabularyFragment.setOnWordSelectedListener((view, index) -> {
            updateMenusVisibility();
        });
        this.vocabularyFragment.setOnMeaningSelectedListener((view, index) -> {
            updateMenusVisibility();
        });

        originalVocabulary = VocabularyMetadata.deserialize(getIntent().getSerializableExtra("vocabulary"));

        if (vocabularyFragment == null) {
            this.vocabularyFragment.setVocabulary(originalVocabulary);
        }

        tagManagerResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::updateVocabulary);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isEdited = savedInstanceState.getBoolean("isEdited");
        isSaved = savedInstanceState.getBoolean("isSaved");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("isEdited", isEdited);
        savedInstanceState.putBoolean("isSaved", isSaved);
    }

    private void searchWord(String query) {
        final String queryLowerCase = query.toLowerCase();
        final Vocabulary searchResult = new Vocabulary();

        for (final Word word : originalVocabulary.getVocabulary().getWords()) {
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

        vocabularyFragment.setWordsTextFormat(getString(R.string.vocabulary_activity_words_search_result));
        vocabularyFragment.setVocabulary(new VocabularyMetadata(query, searchResult));
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

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchWord.setOnCloseListener(() -> {
            vocabularyFragment.setWordsTextFormat(getString(R.string.vocabulary_activity_words));
            vocabularyFragment.setVocabulary(originalVocabulary);

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
            final VocabularyMetadata displayedVocabulary = vocabularyFragment.getVocabulary();
            final Word selectedWord = vocabularyFragment.getSelectedWord();
            final int selectedIndex = vocabularyFragment.getSelectedWordIndex();

            displayedVocabulary.getVocabulary().removeWord(vocabularyFragment.getSelectedWord());
            vocabularyFragment.notifySelectedWordRemoved();

            if (displayedVocabulary != originalVocabulary) {
                originalVocabulary.getVocabulary().removeWord(selectedWord);
            }

            final int wordsCount = displayedVocabulary.getVocabulary().getWords().size();
            if (wordsCount > selectedIndex) {
                vocabularyFragment.setSelectedWord(selectedIndex);
            } else if (wordsCount > 0) {
                vocabularyFragment.setSelectedWord(wordsCount - 1);
            }

            edited();
        }).setNegativeButton(R.string.cancel).show();
    }

    private void deleteMeaning() {
        final Word selectedWord = vocabularyFragment.getSelectedWord();
        if (selectedWord.getMeanings().size() == 1) {
            deleteWord(true);

            return;
        }

        final AlertDialog dialog = new AlertDialog(this,
                R.string.vocabulary_activity_delete_meaning,
                R.string.vocabulary_activity_ask_delete_meaning);

        dialog.setPositiveButton(R.string.delete, true, () -> {
            final Meaning selectedMeaning = vocabularyFragment.getSelectedMeaning();
            final int selectedIndex = vocabularyFragment.getSelectedMeaningIndex();

            selectedWord.removeMeaning(selectedMeaning);
            vocabularyFragment.notifySelectedMeaningRemoved();

            final int meaningsCount = selectedWord.getMeanings().size();
            if (meaningsCount > selectedIndex) {
                vocabularyFragment.setSelectedMeaning(selectedIndex);
            } else if (meaningsCount > 0) {
                vocabularyFragment.setSelectedMeaning(meaningsCount - 1);
            }

            edited();
        }).setNegativeButton(R.string.cancel).show();
    }

    private void replaceWord() {
        final Word selectedWord = vocabularyFragment.getSelectedWord();
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
            vocabularyFragment.notifySelectedWordUpdated();

            edited();

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel).show();
    }

    private void replaceMeaning() {
        final Meaning selectedMeaning = vocabularyFragment.getSelectedMeaning();
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
            } else if (vocabularyFragment.getSelectedWord().containsMeaning(meaning) &&
                    !selectedMeaning.getMeaning().equals(meaning)) {
                Toast.makeText(getApplicationContext(),
                        R.string.vocabulary_activity_error_duplicated_meaning, Toast.LENGTH_SHORT).show();

                return;
            }

            selectedMeaning.setMeaning(meaning);
            vocabularyFragment.notifySelectedMeaningUpdated();

            edited();

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel).show();
    }

    private CheckableAdapter initializeTags(TextView tagsText, Spinner tags) {
        final List<Tag> tagList = originalVocabulary.getVocabulary().getTags();
        if (!tagList.isEmpty()) {
            final CheckableAdapter adapter = TagsSpinner.initializeTags(tags,
                    originalVocabulary.getVocabulary(),
                    getString(R.string.vocabulary_activity_tags_hint),
                    getString(R.string.vocabulary_activity_selected_tags));

            tagsText.setVisibility(View.VISIBLE);
            tags.setVisibility(View.VISIBLE);

            return adapter;
        } else return null;
    }

    private void addTagsToMeaning(Meaning meaning, CheckableAdapter tagsAdapter) {
        if (tagsAdapter == null) return;

        final boolean[] isSelected = tagsAdapter.getIsSelected();

        meaning.removeAllTags();

        for (int i = 0; i < isSelected.length; ++i) {
            if (isSelected[i]) {
                meaning.addTag(originalVocabulary.getVocabulary().getTag(i));
            }
        }
    }

    private void setHints() {
        final Meaning selectedMeaning = vocabularyFragment.getSelectedMeaning();
        final CustomDialog dialog = new CustomDialog(this, R.layout.dialog_set_hints);

        final TextView pronunciation = dialog.findViewById(R.id.pronunciation);
        final TextView example = dialog.findViewById(R.id.example);

        pronunciation.setText(selectedMeaning.getPronunciation());
        example.setText(selectedMeaning.getExample());

        final List<Tag> tags = originalVocabulary.getVocabulary().getTags();
        final CheckableAdapter tagsAdapter = initializeTags(
                dialog.findViewById(R.id.tagsText), dialog.findViewById(R.id.tags));

        if (tagsAdapter != null) {
            final boolean[] isSelected = new boolean[tags.size()];
            for (int i = 0; i < isSelected.length; ++i) {
                isSelected[i] = selectedMeaning.containsTag(tags.get(i));
            }

            tagsAdapter.setIsSelected(isSelected);
        }

        final Button cancel = dialog.findViewById(R.id.cancel);
        final Button change = dialog.findViewById(R.id.change);

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        change.setOnClickListener(v -> {
            final String pronunciationStr = pronunciation.getText().toString().trim();
            final String exampleStr = example.getText().toString().trim();

            if (pronunciationStr.equals(selectedMeaning.getWord().getWord())) {
                Toast.makeText(getApplicationContext(),
                        R.string.vocabulary_activity_warning_pronunciation_equals_word, Toast.LENGTH_SHORT).show();
            }

            selectedMeaning.setPronunciation(pronunciationStr);
            selectedMeaning.setExample(exampleStr);

            addTagsToMeaning(selectedMeaning, tagsAdapter);

            vocabularyFragment.notifyMeaningsUpdated();

            edited();

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showTagManager() {
        final Intent intent = new Intent(this, TagManagerActivity.class);

        intent.putExtra("vocabulary", originalVocabulary.getVocabulary());

        tagManagerResult.launch(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            save();

            return true;
        } else if (item.getItemId() == R.id.deleteWordOrMeaning) {
            if (vocabularyFragment.getSelectedMeaningIndex() == -1) {
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
        } else if (item.getItemId() == R.id.deleteMeaning) {
            deleteMeaning();

            return true;
        } else if (item.getItemId() == R.id.setHints) {
            setHints();

            return true;
        } else if (item.getItemId() == R.id.tagManager) {
            showTagManager();

            return true;
        } else return super.onOptionsItemSelected(item);
    }

    public void onAddClick(View view) {
        final CustomDialog dialog = new CustomDialog(this, R.layout.dialog_add_meaning);

        final TextView word = dialog.findViewById(R.id.word);
        final TextView meaning = dialog.findViewById(R.id.meaning);
        final TextView pronunciation = dialog.findViewById(R.id.pronunciation);
        final TextView example = dialog.findViewById(R.id.example);

        if (vocabularyFragment.getSelectedWordIndex() != -1) {
            word.setText(vocabularyFragment.getSelectedWord().getWord());

            meaning.requestFocus();
        } else {
            word.requestFocus();
        }

        final CheckableAdapter tagsAdapter = initializeTags(
                dialog.findViewById(R.id.tagsText), dialog.findViewById(R.id.tags));

        final Button reset = dialog.findViewById(R.id.reset);
        final Button cancel = dialog.findViewById(R.id.cancel);
        final Button add = dialog.findViewById(R.id.add);

        reset.setOnClickListener(v -> {
            word.setText("");
            meaning.setText("");
            pronunciation.setText("");
            example.setText("");

            if (tagsAdapter != null) {
                tagsAdapter.reset();
            }

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

            final VocabularyMetadata displayedVocabulary = vocabularyFragment.getVocabulary();
            final boolean displayed = displayedVocabulary.getVocabulary().containsWord(targetWord);

            if (!displayed) {
                displayedVocabulary.getVocabulary().addWordRef(targetWord);
            }

            if (!displayed || targetWordIndex == -1) {
                vocabularyFragment.notifyWordAdded();
                vocabularyFragment.setSelectedWord(displayedVocabulary.getVocabulary().getWords().size() - 1);
            } else {
                vocabularyFragment.setSelectedWord(targetWordIndex);
            }

            final Meaning newMeaning = targetWord.addMeaning(new Meaning(
                    meaningStr,
                    pronunciation.getText().toString().trim(),
                    example.getText().toString().trim()));

            addTagsToMeaning(newMeaning, tagsAdapter);

            vocabularyFragment.notifyMeaningAdded();
            vocabularyFragment.setSelectedMeaning(targetWord.getMeanings().size() - 1);

            edited();

            dialog.dismiss();
        });

        dialog.show();
    }
}