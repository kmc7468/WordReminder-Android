package com.staticom.wordreminder.presenter

import android.widget.Toast
import com.staticom.wordreminder.R
import com.staticom.wordreminder.core.VocabularyList
import com.staticom.wordreminder.core.VocabularyMetadata
import org.json.JSONArray
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

interface MainContract {

    interface View : com.staticom.wordreminder.view.View {

        fun updateVocabularyList(vocabularyList: VocabularyList)
    }

    interface Presenter {

        fun initialize(): Boolean

        fun exportVocabulary(uri: URI?)
    }
}

class MainPresenter(
        private val rootPath: Path,
        private val view: MainContract.View) : MainContract.Presenter {

    private lateinit var vocabularyList: VocabularyList
    var selectedVocabulary: VocabularyMetadata? = null

    override fun initialize(): Boolean {
        readVocabularyList()?.let {
            vocabularyList = it
        } ?: return false

        view.updateVocabularyList(vocabularyList)

        return true
    }

    override fun exportVocabulary(uri: URI?) {
        if (uri == null) return

        try {
            val vocabulary = selectedVocabulary!!
            if (!vocabulary.hasVocabulary()) {
                vocabulary.loadVocabulary()
            }

            // TODO: 파일 내보내기

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