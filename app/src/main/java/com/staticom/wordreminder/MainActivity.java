package com.staticom.wordreminder;

import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.staticom.wordreminder.adapter.VocabularyListAdapter;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.VocabularyList;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.utility.AlertDialog;
import com.staticom.wordreminder.utility.CustomDialog;
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
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

    private FloatingActionButton create, load;
    private boolean isOpenAddButtons = false;
    private Animation fabOpen, fabClose;
    private ActivityResultLauncher<String[]> loadVocabularyResult;

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

                    vocabularyList.addVocabulary(vocabulary);
                    vocabularyListAdapter.notifyItemInserted(vocabularyListAdapter.getItemCount());
                }
            } catch (final Exception e) {
                Toast.makeText(getApplicationContext(), R.string.main_activity_load_vocabulary_error, Toast.LENGTH_LONG).show();

                e.printStackTrace();
            }
        });
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
            });
            vocabularyListAdapter.setOnEditButtonClickListener(index -> {
                if (!selectedVocabulary.hasVocabulary()) {
                    try {
                        selectedVocabulary.loadVocabulary();
                    } catch (final Exception e) {
                        Toast.makeText(this, R.string.main_activity_load_vocabulary_error, Toast.LENGTH_LONG).show();

                        e.printStackTrace();
                        return;
                    }
                }

                final Intent intent = new Intent(this, VocabularyActivity.class);

                intent.putExtra("vocabulary", selectedVocabulary.serialize());

                startActivity(intent);
            });

            final RecyclerView vocabularyList = findViewById(R.id.vocabularyList);

            vocabularyList.setLayoutManager(new LinearLayoutManager(this));
            vocabularyList.setAdapter(vocabularyListAdapter);

            vocabularyListAdapter.registerAdapterDataObserver(
                    new RecyclerViewEmptyObserver(vocabularyList, findViewById(R.id.emptyVocabularyListText)));
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
            vocabularyList.removeVocabulary(selectedVocabulary);
            vocabularyListAdapter.notifyItemRemoved(vocabularyListAdapter.getSelectedIndex());
            vocabularyListAdapter.setSelectedIndex(-1);

            setSelectedVocabulary(null);
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
        final StringBuilder versionStringBuilder = new StringBuilder();

        versionStringBuilder.append(getString(R.string.main_activity_current_version));
        versionStringBuilder.append(": ");
        versionStringBuilder.append(BuildConfig.VERSION_NAME);
        versionStringBuilder.append(' ');
        versionStringBuilder.append(BuildConfig.BUILD_TYPE);

        version.setText(versionStringBuilder.toString());

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

        requireVocabularyName(R.string.main_activity_create_vocabulary, R.string.add, false, "", name -> {
            final Path path = rootPath.resolve(UUID.randomUUID().toString() + ".kv");
            final LocalDateTime time = LocalDateTime.now();

            final VocabularyMetadata vocabulary = new VocabularyMetadata(name, path, time);

            vocabulary.setVocabulary(new Vocabulary());
            vocabulary.setShouldSave(true);

            vocabularyList.addVocabulary(vocabulary);
            vocabularyListAdapter.notifyItemInserted(vocabularyListAdapter.getItemCount());
        });
    }

    public void onLoadClick(View view) {
        toggleAddButtons();

        loadVocabularyResult.launch(new String[] { "*/*" });
    }

    public void onAddClick(View view) {
        toggleAddButtons();
    }
}