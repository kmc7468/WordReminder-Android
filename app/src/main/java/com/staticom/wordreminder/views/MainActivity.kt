package com.staticom.wordreminder.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.staticom.wordreminder.BuildConfig
import com.staticom.wordreminder.DetailedVocabularyActivity
import com.staticom.wordreminder.R
import com.staticom.wordreminder.adapter.VocabularyListAdapter
import com.staticom.wordreminder.contracts.MainContract
import com.staticom.wordreminder.core.Vocabulary
import com.staticom.wordreminder.core.VocabularyList
import com.staticom.wordreminder.core.VocabularyMetadata
import com.staticom.wordreminder.databinding.ActivityMainBinding
import com.staticom.wordreminder.presenters.MainPresenter
import com.staticom.wordreminder.utility.AlertDialog
import com.staticom.wordreminder.utility.CustomDialog
import com.staticom.wordreminder.utility.RecyclerViewEmptyObserver
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), MainContract.View {

    private companion object {

        const val KEY_SELECTED_VOCABULARY = "selectedVocabulary"
        const val KEY_RESULT_VOCABULARY = "vocabulary"
        const val KEY_INPUT_VOCABULARY = "vocabulary"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var optionsMenu: Menu
    private lateinit var presenter: MainContract.Presenter

    private lateinit var exportSelectedVocabularyResult: ActivityResultLauncher<String>
    private lateinit var openSelectedVocabularyResult: ActivityResultLauncher<Intent>
    private lateinit var importVocabularyResult: ActivityResultLauncher<Array<String>>
    private lateinit var startResult: ActivityResultLauncher<Intent>

    private lateinit var fabOpenAnimation: Animation
    private lateinit var fabCloseAnimation: Animation
    private var isCreateFabOpen = false
    private var isStartFabOpen = false

    private lateinit var vocabularyListAdapter: VocabularyListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setTitle(R.string.main_activity_title)

        presenter = MainPresenter(filesDir.toPath(), this)
        presenter.start()

        initActivityResultLauncher()
        initAnimation()
        initViewHandler()
    }

    override fun onStop() {
        super.onStop()

        presenter.finish()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        if (::vocabularyListAdapter.isInitialized) {
            vocabularyListAdapter.selectedIndex = savedInstanceState.getInt(KEY_SELECTED_VOCABULARY)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (::vocabularyListAdapter.isInitialized) {
            outState.putInt(KEY_SELECTED_VOCABULARY, vocabularyListAdapter.selectedIndex)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        optionsMenu = menu!!

        menuInflater.inflate(R.menu.menu_main_activity, optionsMenu)

        if (presenter.selectedVocabulary != null) {
            optionsMenu.setGroupVisible(R.id.editMenus, true)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.delete -> {
            AlertDialog(
                this,
                R.string.main_activity_menu_delete,
                R.string.main_activity_ask_delete_vocabulary
            )
                .setPositiveButton(R.string.delete, true) { _ ->
                    presenter.deleteSelectedVocabulary()
                }
                .setNegativeButton(R.string.cancel)
                .show()

            true
        }

        R.id.rename -> {
            askVocabularyName(
                R.string.main_activity_menu_rename,
                R.string.change,
                true,
                presenter.selectedVocabulary!!.name
            ) { name ->
                presenter.renameSelectedVocabulary(name)
            }

            true
        }

        R.id.export -> {
            exportSelectedVocabularyResult.launch("${presenter.selectedVocabulary!!.name}.kv")

            true
        }

        R.id.about -> {
            // TODO: ABOUT 구현

            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun showInfoToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    override fun showErrorToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
    }

    override fun onVocabularyListLoad(vocabularyList: VocabularyList) {
        vocabularyListAdapter = VocabularyListAdapter(vocabularyList)
        vocabularyListAdapter.setOnItemSelectedListener { _, index ->
            presenter.changeSelectedVocabulary(index)

            if (!isStartFabOpen) {
                toggleStartFab()
            }
        }
        vocabularyListAdapter.setOnOpenButtonClickListener { index ->
            if (presenter.loadSelectedVocabulary()) {
                val intent = Intent(this, DetailedVocabularyActivity::class.java)

                intent.putExtra(KEY_INPUT_VOCABULARY, presenter.selectedVocabulary!!.serialize())

                openSelectedVocabularyResult.launch(intent)
            }
        }

        binding.vocabularyList.layoutManager = LinearLayoutManager(this)
        binding.vocabularyList.adapter = vocabularyListAdapter

        vocabularyListAdapter.registerAdapterDataObserver(
            RecyclerViewEmptyObserver(binding.vocabularyList, binding.emptyVocabularyListText)
        )
    }

    override fun onSelectedVocabularyChange(isSelected: Boolean) {
        if (::optionsMenu.isInitialized) {
            optionsMenu.setGroupVisible(R.id.editMenus, isSelected)
        }
    }

    override fun onSelectedVocabularyUpdate() {
        vocabularyListAdapter.notifyItemChanged(vocabularyListAdapter.selectedIndex)
    }

    override fun onSelectedVocabularyDelete() {
        val index = vocabularyListAdapter.selectedIndex

        vocabularyListAdapter.notifyItemRemoved(index)
        vocabularyListAdapter.selectedIndex = -1

        val newIndex = index.coerceAtMost(vocabularyListAdapter.itemCount - 1)
        if (newIndex >= 0) {
            vocabularyListAdapter.selectedIndex = newIndex
        } else {
            toggleStartFab()
        }
    }

    override fun onVocabularyAdd() {
        vocabularyListAdapter.notifyItemInserted(vocabularyListAdapter.itemCount)
        vocabularyListAdapter.selectedIndex = vocabularyListAdapter.itemCount - 1
    }

    override fun warnUnreadableContainers(callback: () -> Unit) {
        AlertDialog(
            this,
            R.string.main_activity_warning_has_unreadable_containers,
            R.string.main_activity_ask_load_vocabulary_which_has_unreadable_containers
        )
            .setPositiveButton(R.string.load, true, callback)
            .setNegativeButton(R.string.cancel)
            .show()
    }

    private fun initActivityResultLauncher() {
        exportSelectedVocabularyResult =
            registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
                uri?.let {
                    contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                        FileOutputStream(pfd.fileDescriptor).use { stream ->
                            presenter.exportSelectedVocabulary(stream)
                        }
                    }
                }
            }
        openSelectedVocabularyResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                result.resultCode.takeUnless { it != RESULT_OK }?.let {
                    val intent = result.data!!
                    val vocabulary = VocabularyMetadata.deserialize(
                        intent.getSerializableExtra(KEY_RESULT_VOCABULARY)
                    ) // TODO: deprecated

                    presenter.updateSelectedVocabulary(vocabulary)
                }
            }
        importVocabularyResult =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let {
                    val filename = getFilenameFromUri(uri).replaceFirst("[.][^.]+$".toRegex(), "")

                    askVocabularyName(
                        R.string.main_activity_import_vocabulary,
                        R.string.add,
                        false,
                        filename
                    ) { name ->
                        contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                            FileInputStream(pfd.fileDescriptor).use { stream ->
                                presenter.importVocabulary(stream, name)
                            }
                        }
                    }
                }
            }
        startResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                result.resultCode.takeUnless { it != RESULT_OK }?.let {
                    val intent = result.data!!
                    val vocabulary = VocabularyMetadata.deserialize(
                        intent.getSerializableExtra(KEY_RESULT_VOCABULARY)
                    ) // TODO: deprecated

                    presenter.createVocabulary(vocabulary.name, vocabulary.vocabulary)
                }
            }
    }

    private fun initAnimation() {
        fabOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fabCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
    }

    private fun initViewHandler() {
        binding.create.setOnClickListener {
            toggleCreateFab()

            askVocabularyName(
                R.string.main_activity_create_vocabulary,
                R.string.add,
                false,
                ""
            ) { name ->
                presenter.createVocabulary(name, Vocabulary())
            }
        }
        binding.load.setOnClickListener {
            toggleCreateFab()

            importVocabularyResult.launch(arrayOf("*/*"))
        }
        binding.add.setOnClickListener {
            toggleCreateFab()
        }
        binding.start.setOnClickListener {
            // TODO: 단어 암기
        }
    }

    private fun getFilenameFromUri(uri: Uri): String =
        contentResolver.query(uri, null, null, null, null)!!.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            it.moveToFirst()
            it.getString(nameIndex)
        }

    private fun askVocabularyName(
        @StringRes messageId: Int, @StringRes positiveButtonTextId: Int,
        allowDuplicate: Boolean, defaultName: String, callback: (String) -> Unit
    ) {
        AlertDialog(
            this,
            messageId,
            R.string.main_activity_require_vocabulary_name
        )
            .addEdit(defaultName)
            .setPositiveButton(positiveButtonTextId, false) { dialog ->
                val name = dialog.editText.trim()
                if (presenter.checkVocabularyNameValidity(name, allowDuplicate)) {
                    callback(name)
                    dialog.dismiss()
                }
            }
            .setNegativeButton(R.string.cancel)
            .show()
    }

    private fun toggleCreateFab() {
        val animation = if (isCreateFabOpen) {
            fabCloseAnimation
        } else {
            fabOpenAnimation
        }

        binding.load.startAnimation(animation)
        binding.create.startAnimation(animation)

        isCreateFabOpen = !isCreateFabOpen

        val visibility = if (isCreateFabOpen) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }

        binding.load.visibility = visibility
        binding.create.visibility = visibility

        binding.load.isClickable = isCreateFabOpen
        binding.create.isClickable = isCreateFabOpen
    }

    private fun toggleStartFab() {
        binding.start.startAnimation(
            if (isStartFabOpen) {
                fabCloseAnimation
            } else {
                fabOpenAnimation
            }
        )

        isStartFabOpen = !isStartFabOpen

        binding.start.visibility = if (isStartFabOpen) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
        binding.start.isClickable = isStartFabOpen
    }
}