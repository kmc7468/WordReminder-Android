package com.staticom.wordreminder.contracts

import com.staticom.wordreminder.core.VocabularyList
import java.io.FileOutputStream

interface MainContract {

    interface View : com.staticom.wordreminder.contracts.View {

        fun updateVocabularyList(vocabularyList: VocabularyList)
    }

    interface Presenter {

        fun initialize(): Boolean

        fun exportVocabulary(stream: FileOutputStream)
    }
}