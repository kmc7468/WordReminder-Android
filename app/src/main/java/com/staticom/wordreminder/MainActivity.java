package com.staticom.wordreminder;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.staticom.wordreminder.adapter.VocabularyListAdapter;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.VocabularyList;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.utility.AlertDialog;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Path rootPath;

    private VocabularyList vocabularyList;
    private VocabularyListAdapter vocabularyListAdapter;
    private VocabularyMetadata selectedVocabulary;

    private FloatingActionButton create, load;
    private boolean isOpenAddButtons = false;
    private Animation fabOpen, fabClose;
    private ActivityResultLauncher<String[]> loadVocabularyResult;

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

        // TODO
    }

    private String getFilenameFromUri(Uri uri) {
        try (final Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            final int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            cursor.moveToFirst();

            return cursor.getString(nameIndex);
        }
    }

    private void loadVocabulary(Uri uri) {
        final String filename = getFilenameFromUri(uri).replaceFirst("[.][^.]+$", "");
        final AlertDialog dialog = new AlertDialog(this,
                R.string.main_activity_load_vocabulary,
                R.string.main_activity_require_vocabulary_name);

        dialog.addEdit(filename);
        dialog.setPositiveButton(R.string.add, false, () -> {
            final String name = dialog.getEditText().trim();
            final Path path = rootPath.resolve(UUID.randomUUID().toString() + ".kv");
            final LocalDateTime time = LocalDateTime.now();

            if (name.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        R.string.main_activity_error_empty_vocabulary_name, Toast.LENGTH_LONG).show();

                return;
            } else if (vocabularyList.containsVocabulary(name)) {
                Toast.makeText(getApplicationContext(),
                        R.string.main_activity_error_duplicated_vocabulary_name, Toast.LENGTH_LONG).show();

                return;
            }

            try {
                try (final ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                     final FileInputStream stream = new FileInputStream(pfd.getFileDescriptor())) {
                    final VocabularyMetadata vocabulary = new VocabularyMetadata(name, path, time);

                    vocabulary.setVocabulary(Vocabulary.readFromFileStream(stream));
                    vocabulary.setShouldSave(true);

                    vocabularyList.addVocabulary(vocabulary);
                    vocabularyListAdapter.notifyItemInserted(vocabularyListAdapter.getItemCount());
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.main_activity_load_vocabulary_error, Toast.LENGTH_LONG).show();

                e.printStackTrace();
            }

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.main_activity_title);

        rootPath = getFilesDir().toPath();

        if (readVocabularyList()) {
            vocabularyListAdapter = new VocabularyListAdapter(vocabularyList);

            vocabularyListAdapter.setOnItemSelectedListener((view, index) -> {
                setSelectedVocabulary(vocabularyList.getVocabulary(index));
            });
            vocabularyListAdapter.setOnEditButtonClickListener(index -> {
                // TODO
            });

            final RecyclerView vocabularyList = findViewById(R.id.vocabularyList);

            vocabularyList.setLayoutManager(new LinearLayoutManager(this));
            vocabularyList.setAdapter(vocabularyListAdapter);
        }

        create = findViewById(R.id.create);
        load = findViewById(R.id.load);

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        loadVocabularyResult = registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::loadVocabulary);
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

    private void writeVocabularyList() {
        try {
            vocabularyList.saveVocabulary();

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
            load.startAnimation(fabClose);
            create.startAnimation(fabClose);
        } else {
            load.startAnimation(fabOpen);
            create.startAnimation(fabOpen);
        }

        isOpenAddButtons = !isOpenAddButtons;

        create.setVisibility(isOpenAddButtons ? View.VISIBLE : View.INVISIBLE);
        create.setClickable(isOpenAddButtons);

        load.setVisibility(isOpenAddButtons ? View.VISIBLE : View.INVISIBLE);
        load.setClickable(isOpenAddButtons);
    }

    public void onCreateClick(View view) {
        toggleAddButtons();

        // TODO
    }

    public void onLoadClick(View view) {
        toggleAddButtons();

        loadVocabularyResult.launch(new String[] { "*/*" });
    }

    public void onAddClick(View view) {
        toggleAddButtons();
    }
}