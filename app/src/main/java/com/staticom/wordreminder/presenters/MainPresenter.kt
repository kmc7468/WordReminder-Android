package com.staticom.wordreminder.presenters

import android.widget.Toast
import com.staticom.wordreminder.R
import com.staticom.wordreminder.contracts.MainContract
import com.staticom.wordreminder.core.VocabularyList
import com.staticom.wordreminder.core.VocabularyMetadata
import org.json.JSONArray
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class MainPresenter(
    private val rootPath: Path,
    private val view: MainContract.View
) : MainContract.Presenter {

    private lateinit var vocabularyList: VocabularyList
    var selectedVocabulary: VocabularyMetadata? = null

    override fun initialize(): Boolean {
        readVocabularyList()?.let {
            vocabularyList = it
        } ?: return false

        view.updateVocabularyList(vocabularyList)

        return true
    }

    override fun exportVocabulary(stream: FileOutputStream) {
        try {
            val vocabulary = selectedVocabulary!!
            if (!vocabulary.hasVocabulary()) {
                vocabulary.loadVocabulary()
            }

            vocabulary.vocabulary.writeToFileStream(stream);

            view.showToast(R.string.main_activity_success_export_vocabulary, Toast.LENGTH_SHORT)
        } catch (e: Exception) {
            e.printStackTrace()
            view.showToast(R.string.main_activity_error_export_vocabulary, Toast.LENGTH_LONG)
        }
    }

    private fun readVocabularyList(): VocabularyList? = try {
        val jsonPath = rootPath.resolve("vocabularyList.json")
        if (jsonPath.exists()) {
            val jsonBytes = Files.readAllBytes(jsonPath)
            val jsonArray = JSONArray(jsonBytes.toString())

            VocabularyList.loadFromJSONArray(jsonArray, rootPath)
        } else {
            VocabularyList()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        view.showToast(R.string.main_activity_error_read_vocabulary_list, Toast.LENGTH_LONG)

        null
    }
}