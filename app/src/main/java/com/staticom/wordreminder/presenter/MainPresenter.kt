package com.staticom.wordreminder.presenter

import com.staticom.wordreminder.core.VocabularyList
import org.json.JSONArray
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

interface MainContract {

    interface View {

        fun updateVocabularyList(vocabularyList: VocabularyList)
    }

    interface Presenter {

        fun initialize(): Boolean
    }
}

class MainPresenter(
        private val rootPath: Path,
        private val view: MainContract.View) : MainContract.Presenter {

    private lateinit var vocabularyList: VocabularyList

    override fun initialize(): Boolean {
        readVocabularyList()?.let {
            vocabularyList = it
        } ?: return false

        view.updateVocabularyList(vocabularyList)

        return true
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
        null // TODO: 오류 보고
    }
}