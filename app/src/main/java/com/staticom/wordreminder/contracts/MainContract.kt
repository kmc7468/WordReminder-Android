package com.staticom.wordreminder.contracts

import com.staticom.wordreminder.core.VocabularyList
import com.staticom.wordreminder.core.VocabularyMetadata
import java.io.FileInputStream
import java.io.FileOutputStream

interface MainContract {

    interface View : com.staticom.wordreminder.contracts.View {

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
        fun exportSelectedVocabulary(stream: FileOutputStream)
        fun updateSelectedVocabulary(vocabulary: VocabularyMetadata)
        fun deleteSelectedVocabulary()

        fun importVocabulary(stream: FileInputStream, name: String)
    }
}