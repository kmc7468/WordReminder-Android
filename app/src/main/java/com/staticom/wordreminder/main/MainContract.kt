package com.staticom.wordreminder.main

import com.staticom.wordreminder.BaseView
import com.staticom.wordreminder.core.Vocabulary
import com.staticom.wordreminder.core.VocabularyList
import com.staticom.wordreminder.core.VocabularyMetadata
import java.io.FileInputStream
import java.io.FileOutputStream

interface MainContract {

    interface View : BaseView {

        fun onVocabularyListLoad(vocabularyList: VocabularyList)
        fun onSelectedVocabularyChange(isSelected: Boolean)
        fun onSelectedVocabularyUpdate()
        fun onSelectedVocabularyDelete()

        fun onVocabularyAdd()

        fun warnUnreadableContainers(callback: () -> Unit)
    }

    interface Presenter {

        val selectedVocabulary: VocabularyMetadata?

        fun start(): Boolean
        fun finish()

        fun changeSelectedVocabulary(index: Int)
        fun loadSelectedVocabulary(): Boolean
        fun exportSelectedVocabulary(stream: FileOutputStream)
        fun updateSelectedVocabulary(vocabulary: VocabularyMetadata)
        fun deleteSelectedVocabulary()
        fun renameSelectedVocabulary(name: String)

        fun createVocabulary(name: String, vocabulary: Vocabulary)
        fun importVocabulary(stream: FileInputStream, name: String)
        fun checkVocabularyNameValidity(name: String, allowDuplicate: Boolean): Boolean
    }
}