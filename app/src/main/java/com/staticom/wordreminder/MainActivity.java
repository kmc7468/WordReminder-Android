package com.staticom.wordreminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.staticom.wordreminder.adapter.CheckableAdapter;
import com.staticom.wordreminder.adapter.VocabularyListAdapter;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Tag;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.VocabularyList;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;
import com.staticom.wordreminder.utility.AlertDialog;
import com.staticom.wordreminder.utility.CustomDialog;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;
import com.staticom.wordreminder.utility.TagsSpinner;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private interface OnVocabularyNameInputted {
        void onVocabularyNameInputted(String name);
    }

    private Menu menu;

    private ActivityResultLauncher<String> exportVocabularyResult;

    private Path rootPath;

    private VocabularyList vocabularyList;
    private VocabularyListAdapter vocabularyListAdapter;
    private VocabularyMetadata selectedVocabulary;
    private ActivityResultLauncher<Intent> openVocabularyResult;

    private FloatingActionButton create, load;
    private boolean isOpenAddButtons = false;
    private Animation createOpenAnimation, createCloseAnimation;
    private ActivityResultLauncher<String[]> loadVocabularyResult;

    private FloatingActionButton start;
    private boolean isOpenStartButton = false;
    private Animation startOpenAnimation, startCloseAnimation;
    private ActivityResultLauncher<Intent> startResult;

    private void exportVocabulary(Uri uri) {
        if (uri == null) return;

        try {
            if (!selectedVocabulary.hasVocabulary()) {
                selectedVocabulary.loadVocabulary();
            }

            try (final ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
                 final FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor())) {
                selectedVocabulary.getVocabulary().writeToFileStream(outputStream);
            }

            Toast.makeText(getApplicationContext(), R.string.main_activity_success_export_vocabulary, Toast.LENGTH_SHORT).show();
        } catch (final Exception e) {
            Toast.makeText(getApplicationContext(), R.string.main_activity_error_export_vocabulary, Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
    }

    private boolean readVocabularyList() {
        try {
            final Path path = rootPath.resolve("vocabularyList.json");
            if (Files.exists(path)) {
                final byte[] bytes = Files.readAllBytes(path);
                final JSONArray array = new JSONArray(new String(bytes));

                vocabularyList = VocabularyList.loadFromJSONArray(array, rootPath);
            } else {
                vocabularyList = new VocabularyList();
            }

            return true;
        } catch (final Exception e) {
            vocabularyList = null;

            Toast.makeText(this, R.string.main_activity_error_read_vocabulary_list, Toast.LENGTH_LONG).show();

            e.printStackTrace();
            return false;
        }
    }

    private void setSelectedVocabulary(VocabularyMetadata vocabulary) {
        selectedVocabulary = vocabulary;

        if (menu != null) {
            menu.setGroupVisible(R.id.editMenus, vocabulary != null);
        }
    }

    private void toggleStartButton() {
        if (isOpenStartButton) {
            start.startAnimation(startCloseAnimation);
        } else {
            start.startAnimation(startOpenAnimation);
        }

        isOpenStartButton = !isOpenStartButton;

        start.setVisibility(isOpenStartButton ? View.VISIBLE : View.INVISIBLE);
        start.setClickable(isOpenStartButton);
    }

    private boolean loadVocabulary(VocabularyMetadata vocabulary) {
        if (vocabulary.hasVocabulary()) return true;

        try {
            //Toast.makeText(this, "단어장로드시작" + LocalDateTime.now().toString(), Toast.LENGTH_SHORT).show();
            vocabulary.loadVocabulary();
            //Toast.makeText(this, "단어장로드완료" + LocalDateTime.now().toString(), Toast.LENGTH_SHORT).show();

            return true;
        } catch (final Exception e) {
            Toast.makeText(this, R.string.main_activity_load_vocabulary_error, Toast.LENGTH_LONG).show();

            e.printStackTrace();
            return false;
        }
    }

    private void updateVocabulary(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK) return;

        final Intent intent = result.getData();
        final VocabularyMetadata vocabulary = VocabularyMetadata.deserialize(intent.getSerializableExtra("vocabulary"));

        selectedVocabulary.setVocabulary(vocabulary.getVocabulary());
        //selectedVocabulary.setTime(vocabulary.getTime()); TODO

        vocabularyListAdapter.notifyItemChanged(vocabularyListAdapter.getSelectedIndex());
    }

    private String getFilenameFromUri(Uri uri) {
        try (final Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            final int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            cursor.moveToFirst();

            return cursor.getString(nameIndex);
        }
    }

    private void requireVocabularyName(@StringRes int messageId, @StringRes int positiveButtonTextId, boolean allowDuplicatedWithSelectedVocabulary,
                                       String defaultName, OnVocabularyNameInputted onVocabularyNameInputted) {
        final AlertDialog dialog = new AlertDialog(this, messageId,
                R.string.main_activity_require_vocabulary_name);

        dialog.addEdit(defaultName);
        dialog.setPositiveButton(positiveButtonTextId, false, () -> {
            final String name = dialog.getEditText().trim();
            if (name.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        R.string.main_activity_error_empty_vocabulary_name, Toast.LENGTH_SHORT).show();

                return;
            } else if (vocabularyList.containsVocabulary(name) &&
                    (!allowDuplicatedWithSelectedVocabulary || !selectedVocabulary.getName().equals(name))) {
                Toast.makeText(getApplicationContext(),
                        R.string.main_activity_error_duplicated_vocabulary_name, Toast.LENGTH_SHORT).show();

                return;
            }

            onVocabularyNameInputted.onVocabularyNameInputted(name);

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel).show();
    }

    private void loadVocabulary(Uri uri) {
        if (uri == null) return;

        final String filename = getFilenameFromUri(uri).replaceFirst("[.][^.]+$", "");

        requireVocabularyName(R.string.main_activity_load_vocabulary, R.string.add, false, filename, name -> {
            final Path path = rootPath.resolve(UUID.randomUUID().toString() + ".kv");
            final LocalDateTime time = LocalDateTime.now();

            try {
                try (final ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                     final FileInputStream stream = new FileInputStream(pfd.getFileDescriptor())) {
                    final VocabularyMetadata vocabulary = new VocabularyMetadata(name, path, time);

                    vocabulary.setVocabulary(Vocabulary.readFromFileStream(stream));
                    vocabulary.setShouldSave(true);

                    if (vocabulary.getVocabulary().hasUnreadableContainers()) {
                        final AlertDialog dialog = new AlertDialog(this,
                                R.string.main_activity_warning_has_unreadable_containers,
                                R.string.main_activity_ask_load_vocabulary_which_has_unreadable_containers);

                        dialog.setPositiveButton(R.string.load, true, () -> {
                            vocabularyList.addVocabulary(vocabulary);
                            vocabularyListAdapter.notifyItemInserted(vocabularyListAdapter.getItemCount());
                            vocabularyListAdapter.setSelectedIndex(vocabularyListAdapter.getItemCount() - 1);
                        }).setNegativeButton(R.string.cancel).show();
                    } else {
                        vocabularyList.addVocabulary(vocabulary);
                        vocabularyListAdapter.notifyItemInserted(vocabularyListAdapter.getItemCount());
                        vocabularyListAdapter.setSelectedIndex(vocabularyListAdapter.getItemCount() - 1);
                    }
                }
            } catch (final Exception e) {
                Toast.makeText(getApplicationContext(), R.string.main_activity_load_vocabulary_error, Toast.LENGTH_LONG).show();

                e.printStackTrace();
            }
        });
    }

    private void addVocabularyForWrongAnswers(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK) return;

        final Intent intent = result.getData();
        final VocabularyMetadata vocabulary = VocabularyMetadata.deserialize(intent.getSerializableExtra("vocabulary"));

        vocabularyList.addVocabulary(vocabulary);
        vocabularyListAdapter.notifyItemInserted(vocabularyListAdapter.getItemCount());
        vocabularyListAdapter.setSelectedIndex(vocabularyListAdapter.getItemCount() - 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.main_activity_title);

        exportVocabularyResult = registerForActivityResult(new ActivityResultContracts.CreateDocument(), this::exportVocabulary);

        rootPath = getFilesDir().toPath();

        if (readVocabularyList()) {
            vocabularyListAdapter = new VocabularyListAdapter(vocabularyList);

            vocabularyListAdapter.setOnItemSelectedListener((view, index) -> {
                setSelectedVocabulary(vocabularyList.getVocabulary(index));

                if (!isOpenStartButton) {
                    toggleStartButton();
                }
            });
            vocabularyListAdapter.setOnOpenButtonClickListener(index -> {
                //Toast.makeText(this, "이벤트인식" + LocalDateTime.now().toString(), Toast.LENGTH_SHORT).show();

                if (!loadVocabulary(selectedVocabulary)) return;

                final Intent intent = new Intent(this, DetailedVocabularyActivity.class);

                //Toast.makeText(this, "인텐트생성완료" + LocalDateTime.now().toString(), Toast.LENGTH_SHORT).show();
                intent.putExtra("vocabulary", selectedVocabulary.serialize());
                //Toast.makeText(this, "인텐트탑재완료" + LocalDateTime.now().toString(), Toast.LENGTH_SHORT).show();

                openVocabularyResult.launch(intent);
            });

            final RecyclerView vocabularyList = findViewById(R.id.vocabularyList);

            vocabularyList.setLayoutManager(new LinearLayoutManager(this));
            vocabularyList.setAdapter(vocabularyListAdapter);

            vocabularyListAdapter.registerAdapterDataObserver(
                    new RecyclerViewEmptyObserver(vocabularyList, findViewById(R.id.emptyVocabularyListText)));

            openVocabularyResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::updateVocabulary);
        }

        create = findViewById(R.id.create);
        load = findViewById(R.id.load);
        createOpenAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        createCloseAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        loadVocabularyResult = registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::loadVocabulary);

        start = findViewById(R.id.start);
        startOpenAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        startCloseAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        startResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::addVocabularyForWrongAnswers);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (vocabularyListAdapter != null) {
            vocabularyListAdapter.setSelectedIndex(savedInstanceState.getInt("selectedVocabulary"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (vocabularyListAdapter != null) {
            savedInstanceState.putInt("selectedVocabulary", vocabularyListAdapter.getSelectedIndex());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);

        this.menu = menu;

        if (selectedVocabulary != null) {
            menu.setGroupVisible(R.id.editMenus, true);
        }

        return true;
    }

    private void deleteVocabulary() {
        final AlertDialog dialog = new AlertDialog(this,
                R.string.main_activity_menu_delete,
                R.string.main_activity_ask_delete_vocabulary);

        dialog.setPositiveButton(R.string.delete, true, () -> {
            final int selectedIndex = vocabularyListAdapter.getSelectedIndex();

            vocabularyList.removeVocabulary(selectedVocabulary);
            vocabularyListAdapter.notifyItemRemoved(selectedIndex);
            vocabularyListAdapter.setSelectedIndex(-1);

            if (vocabularyListAdapter.getItemCount() > selectedIndex) {
                vocabularyListAdapter.setSelectedIndex(selectedIndex);
            } else if (vocabularyListAdapter.getItemCount() > 0) {
                vocabularyListAdapter.setSelectedIndex(vocabularyListAdapter.getItemCount() - 1);
            } else {
                setSelectedVocabulary(null);
                toggleStartButton();
            }
        }).setNegativeButton(R.string.cancel).show();
    }

    private void renameVocabulary() {
        requireVocabularyName(R.string.main_activity_menu_rename, R.string.change, true, selectedVocabulary.getName(), name -> {
            selectedVocabulary.setName(name);
            vocabularyListAdapter.notifyItemChanged(vocabularyListAdapter.getSelectedIndex());
        });
    }

    private void exportVocabulary() {
        exportVocabularyResult.launch(selectedVocabulary.getName() + ".kv");
    }

    private void showAboutDialog() {
        final CustomDialog dialog = new CustomDialog(this, R.layout.dialog_about);

        final TextView version = dialog.findViewById(R.id.version);
        final StringBuilder versionTextBuilder = new StringBuilder();

        versionTextBuilder.append(getString(R.string.main_activity_current_version));
        versionTextBuilder.append(": ");
        versionTextBuilder.append(BuildConfig.VERSION_NAME);
        versionTextBuilder.append(' ');
        versionTextBuilder.append(BuildConfig.BUILD_TYPE);

        version.setText(versionTextBuilder.toString());

        final Button developerBlog = dialog.findViewById(R.id.developerBlog);
        final Button close = dialog.findViewById(R.id.close);

        developerBlog.setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.naver.com/kmc7468")));

            dialog.dismiss();
        });
        close.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            deleteVocabulary();

            return true;
        } else if (item.getItemId() == R.id.rename) {
            renameVocabulary();

            return true;
        } else if (item.getItemId() == R.id.export) {
            exportVocabulary();

            return true;
        } else if (item.getItemId() == R.id.about) {
            showAboutDialog();

            return true;
        } else return super.onOptionsItemSelected(item);
    }

    private void writeVocabularyList() {
        try {
            vocabularyList.saveOrDeleteVocabulary();

            final Path path = rootPath.resolve("vocabularyList.json");
            final JSONArray array = vocabularyList.saveToJSONArray();

            Files.write(path, array.toString().getBytes());
        } catch (final Exception e) {
            Toast.makeText(this, R.string.main_activity_error_write_vocabulary_list, Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        writeVocabularyList();
    }

    private void toggleAddButtons() {
        if (isOpenAddButtons) {
            load.startAnimation(createCloseAnimation);
            create.startAnimation(createCloseAnimation);
        } else {
            load.startAnimation(createOpenAnimation);
            create.startAnimation(createOpenAnimation);
        }

        isOpenAddButtons = !isOpenAddButtons;

        create.setVisibility(isOpenAddButtons ? View.VISIBLE : View.INVISIBLE);
        create.setClickable(isOpenAddButtons);

        load.setVisibility(isOpenAddButtons ? View.VISIBLE : View.INVISIBLE);
        load.setClickable(isOpenAddButtons);
    }

    public void onCreateClick(View view) {
        toggleAddButtons();

        requireVocabularyName(R.string.main_activity_create_vocabulary, R.string.add, false, "", name -> {
            final Path path = rootPath.resolve(UUID.randomUUID().toString() + ".kv");
            final LocalDateTime time = LocalDateTime.now();

            final VocabularyMetadata vocabulary = new VocabularyMetadata(name, path, time);

            vocabulary.setVocabulary(new Vocabulary());
            vocabulary.setShouldSave(true);

            vocabularyList.addVocabulary(vocabulary);
            vocabularyListAdapter.notifyItemInserted(vocabularyListAdapter.getItemCount());
            vocabularyListAdapter.setSelectedIndex(vocabularyListAdapter.getItemCount() - 1);
        });
    }

    public void onLoadClick(View view) {
        toggleAddButtons();

        loadVocabularyResult.launch(new String[] { "*/*" });
    }

    public void onAddClick(View view) {
        toggleAddButtons();
    }

    private VocabularyMetadata makeVocabulary(Switch selectTags, CheckableAdapter tagsAdapter) {
        if (!selectTags.isChecked()) return selectedVocabulary;

        final boolean[] isSelected = tagsAdapter.getIsSelected();
        final List<Tag> selectedTags = new ArrayList<>();

        for (int i = 0; i < isSelected.length; ++i) {
            if (isSelected[i]) {
                selectedTags.add(selectedVocabulary.getVocabulary().getTag(i));
            }
        }

        final VocabularyMetadata vocabulary = new VocabularyMetadata(selectedVocabulary.getName(), null, null);
        final Vocabulary taggedVocabulary = new Vocabulary();

        for (final Tag tag : selectedVocabulary.getVocabulary().getTags()) {
            taggedVocabulary.addTag(tag);
        }

        for (final Word word : selectedVocabulary.getVocabulary().getWords()) {
            final Word wordRef = new Word(word.getWord());

            for (final Meaning meaning : word.getMeanings()) {
                if (meaning.containsTag(selectedTags)) {
                    wordRef.addMeaningRef(meaning);
                }
            }

            if (!wordRef.getMeanings().isEmpty()) {
                taggedVocabulary.addWord(wordRef);
            }
        }

        vocabulary.setVocabulary(taggedVocabulary);

        return vocabulary;
    }

    public void onStartClick(View view) {
        if (isOpenAddButtons) {
            toggleAddButtons();
        }

        if (!loadVocabulary(selectedVocabulary)) return;

        final CustomDialog dialog = new CustomDialog(this, R.layout.dialog_question_context);

        final Switch wordToMeaning = dialog.findViewById(R.id.word_to_meaning);
        final Switch wordToMeaningSA = dialog.findViewById(R.id.word_to_meaning_sa);
        final Switch meaningToWord = dialog.findViewById(R.id.meaning_to_word);
        final Switch meaningToWordSA = dialog.findViewById(R.id.meaning_to_word_sa);

        final Switch displayPronunciation = dialog.findViewById(R.id.display_pronunciation);
        final Switch displayExample = dialog.findViewById(R.id.display_example);
        final Switch disableDuplication = dialog.findViewById(R.id.disableDuplication);

        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        wordToMeaning.setChecked(preferences.getBoolean("wordToMeaning", false));
        wordToMeaningSA.setChecked(preferences.getBoolean("wordToMeaningSA", false));
        meaningToWord.setChecked(preferences.getBoolean("meaningToWord", false));
        meaningToWordSA.setChecked(preferences.getBoolean("meaningToWordSA", false));

        displayPronunciation.setChecked(preferences.getBoolean("displayPronunciation", false));
        displayExample.setChecked(preferences.getBoolean("displayExample", false));
        disableDuplication.setChecked(preferences.getBoolean("disableDuplication", false));

        final Switch selectTags = dialog.findViewById(R.id.selectTags);
        final CheckableAdapter tagsAdapter;

        if (!selectedVocabulary.getVocabulary().getTags().isEmpty()) {
            final Spinner tags = dialog.findViewById(R.id.tags);

            tagsAdapter = TagsSpinner.initializeTags(tags,
                    selectedVocabulary.getVocabulary(),
                    getString(R.string.main_activity_tags_hint),
                    getString(R.string.main_activity_selected_tags));

            selectTags.setOnCheckedChangeListener((v, isChecked) -> {
                tags.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
            selectTags.setVisibility(View.VISIBLE);
        } else {
            tagsAdapter = null;
        }

        final Button cancel = dialog.findViewById(R.id.cancel);
        final Button start = dialog.findViewById(R.id.start);

        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        start.setOnClickListener(v -> {
            if (!wordToMeaning.isChecked() && !wordToMeaningSA.isChecked() &&
                    !meaningToWord.isChecked() && !meaningToWordSA.isChecked()) {
                Toast.makeText(getApplicationContext(),
                        R.string.main_activity_question_error_not_selected_question_types, Toast.LENGTH_SHORT).show();

                return;
            }

            final Intent intent = new Intent(this, QuestionActivity.class);

            intent.putExtra("vocabulary", makeVocabulary(selectTags, tagsAdapter).serialize());

            intent.putExtra("wordToMeaning", wordToMeaning.isChecked());
            intent.putExtra("wordToMeaningSA", wordToMeaningSA.isChecked());
            intent.putExtra("meaningToWord", meaningToWord.isChecked());
            intent.putExtra("meaningToWordSA", meaningToWordSA.isChecked());

            intent.putExtra("displayPronunciation", displayPronunciation.isChecked());
            intent.putExtra("displayExample", displayExample.isChecked());
            intent.putExtra("disableDuplication", disableDuplication.isChecked());

            startResult.launch(intent);

            final SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean("wordToMeaning", wordToMeaning.isChecked());
            editor.putBoolean("wordToMeaningSA", wordToMeaningSA.isChecked());
            editor.putBoolean("meaningToWord", meaningToWord.isChecked());
            editor.putBoolean("meaningToWordSA", meaningToWordSA.isChecked());

            editor.putBoolean("displayPronunciation", displayPronunciation.isChecked());
            editor.putBoolean("displayExample", displayExample.isChecked());
            editor.putBoolean("disableDuplication", disableDuplication.isChecked());

            editor.apply();

            dialog.dismiss();
        });

        dialog.show();
    }
}