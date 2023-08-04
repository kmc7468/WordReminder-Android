package com.staticom.wordreminder.presenters

import com.staticom.wordreminder.R
import com.staticom.wordreminder.contracts.MainContract
import com.staticom.wordreminder.core.Vocabulary
import com.staticom.wordreminder.core.VocabularyList
import com.staticom.wordreminder.core.VocabularyMetadata
import org.json.JSONArray
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.UUID
import kotlin.io.path.exists
import kotlin.io.path.notExists

class MainPresenter(
    private val rootPath: Path,
    private val view: MainContract.View
) : MainContract.Presenter {

    private lateinit var vocabularyList: VocabularyList
    private val vocabularyListPath = rootPath.resolve("vocabularyList.json")

    override var selectedVocabulary: VocabularyMetadata? = null

    override fun start(): Boolean {
        if (!readVocabularyList()) return false

        view.onVocabularyListLoad(vocabularyList)

        return true
    }

    override fun finish() {
        writeVocabularyList()
    }

    override fun changeSelectedVocabulary(index: Int) {
        selectedVocabulary = if (index != -1) {
            vocabularyList.getVocabulary(index)
        } else {
            null
        }

        view.onSelectedVocabularyChange(index != -1)
    }

    override fun loadSelectedVocabulary(): Boolean {
        val vocabulary = selectedVocabulary!!
        if (vocabulary.hasVocabulary()) return true

        return try {
            vocabulary.loadVocabulary()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            view.showErrorToast(R.string.main_activity_import_vocabulary_error)

            false
        }
    }

    override fun exportSelectedVocabulary(stream: FileOutputStream) {
        try {
            val vocabulary = selectedVocabulary!!
            if (!vocabulary.hasVocabulary()) {
                vocabulary.loadVocabulary()
            }

            vocabulary.vocabulary.writeToFileStream(stream);

            view.showInfoToast(R.string.main_activity_success_export_vocabulary)
        } catch (e: Exception) {
            e.printStackTrace()
            view.showErrorToast(R.string.main_activity_error_export_vocabulary)
        }
    }

    override fun updateSelectedVocabulary(vocabulary: VocabularyMetadata) {
        selectedVocabulary!!.vocabulary = vocabulary.vocabulary
        // TODO: 시간 구현

        view.onSelectedVocabularyUpdate()
    }

    override fun deleteSelectedVocabulary() {
        vocabularyList.removeVocabulary(selectedVocabulary)
        view.onSelectedVocabularyDelete()
    }

    override fun renameSelectedVocabulary(name: String) {
        selectedVocabulary!!.name = name
    }

    override fun createVocabulary(name: String, vocabulary: Vocabulary) {
        val path = generateRandomPath()
        val time = LocalDateTime.now()
        val vocabularyMetadata = VocabularyMetadata(name, path, time)

        vocabularyMetadata.vocabulary = vocabulary
        vocabularyMetadata.setShouldSave(true)

        vocabularyList.addVocabulary(vocabularyMetadata)
        view.onVocabularyAdd()
    }

    override fun importVocabulary(stream: FileInputStream, name: String) {
        try {
            val vocabulary = Vocabulary.readFromFileStream(stream)

            if (vocabulary.hasUnreadableContainers()) {
                view.warnUnreadableContainers {
                    createVocabulary(name, vocabulary)
                }
            } else {
                createVocabulary(name, vocabulary)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            view.showErrorToast(R.string.main_activity_import_vocabulary_error)
        }
    }

    override fun checkVocabularyNameValidity(name: String, allowDuplicate: Boolean): Boolean =
        if (name.isEmpty()) {
            view.showInfoToast(R.string.main_activity_error_empty_vocabulary_name)

            false
        } else if (vocabularyList.containsVocabulary(name) && (!allowDuplicate || selectedVocabulary!!.name != name)) {
            view.showInfoToast(R.string.main_activity_error_duplicated_vocabulary_name)

            false
        } else {
            true
        }

    private fun readVocabularyList(): Boolean = try {
        vocabularyList = if (vocabularyListPath.exists()) {
            val jsonBytes = Files.readAllBytes(vocabularyListPath)
            val jsonArray = JSONArray(String(jsonBytes))

            VocabularyList.loadFromJSONArray(jsonArray, rootPath)
        } else {
            VocabularyList()
        }

        true
    } catch (e: Exception) {
        e.printStackTrace()
        view.showErrorToast(R.string.main_activity_error_read_vocabulary_list)

        false
    }

    private fun writeVocabularyList() {
        try {
            vocabularyList.saveAndDeleteVocabulary()

            val jsonArray = vocabularyList.saveToJSONArray()
            val jsonBytes = jsonArray.toString().toByteArray()

            Files.write(vocabularyListPath, jsonBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            view.showErrorToast(R.string.main_activity_error_write_vocabulary_list)
        }
    }

    private fun generateRandomPath(): Path =
        generateSequence { UUID.randomUUID().toString() }
            .map { rootPath.resolve("$it.kv") }
            .first { it.notExists() }
}