package com.staticom.wordreminder.adapter;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.VocabularyMetadata;
import com.staticom.wordreminder.core.Word;

public class DetailedWordsAdapter extends SelectableAdapter {

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final ConstraintLayout wordAndPronunciations;
        private final Animation wordAndPronunciationsOpenAnimation, wordAndPronunciationsCloseAnimation;

        private final TextView word;
        private boolean hasPronunciations;
        private final TextView pronunciations;
        private final Animation pronunciationsOpenAnimation, pronunciationsCloseAnimation;

        private final ConstraintLayout meaningsAndExamples;
        private final Animation meaningsAndExamplesOpenAnimation, meaningsAndExamplesCloseAnimation;

        private final TextView meanings;
        private boolean hasExamples;
        private final TextView examples;
        private final Animation examplesOpenAnimation, examplesCloseAnimation;

        public ViewHolder(Context applicationContext, View view) {
            super(view);

            wordAndPronunciations = view.findViewById(R.id.wordAndPronunciations);
            wordAndPronunciationsOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_open);
            wordAndPronunciationsCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_close);

            word = view.findViewById(R.id.word);
            pronunciations = view.findViewById(R.id.pronunciations);
            pronunciationsOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_open);
            pronunciationsCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_close);

            meaningsAndExamples = view.findViewById(R.id.meaningsAndExamples);
            meaningsAndExamplesOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_open);
            meaningsAndExamplesCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_close);

            meanings = view.findViewById(R.id.meanings);
            examples = view.findViewById(R.id.examples);
            examplesOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_open);
            examplesCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_close);
        }
    }

    private final VocabularyMetadata vocabulary;
    private boolean shouldHideWord = false;
    private boolean shouldHideMeanings = false;
    private boolean shouldHideHints = false;

    public DetailedWordsAdapter(VocabularyMetadata vocabulary) {
        super(R.layout.item_detailed_word);

        this.vocabulary = vocabulary;
    }

    public VocabularyMetadata getVocabulary() {
        return vocabulary;
    }

    @Override
    public int getItemCount() {
        return vocabulary.getVocabulary().getWords().size();
    }

    public Word getSelectedWord() {
        return vocabulary.getVocabulary().getWord(getSelectedIndex());
    }

    @Override
    protected SelectableAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view.getContext().getApplicationContext(), view);
    }

    @Override
    public void onBindViewHolder(SelectableAdapter.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        final ViewHolder myViewHolder = (ViewHolder)viewHolder;

        myViewHolder.wordAndPronunciations.clearAnimation();
        myViewHolder.pronunciations.clearAnimation();
        myViewHolder.meaningsAndExamples.clearAnimation();
        myViewHolder.examples.clearAnimation();

        final Word word = vocabulary.getVocabulary().getWord(position);
        final Meaning mergedMeaning = word.mergeMeanings(", ", ", ", "\n");

        myViewHolder.word.setText(word.getWord());
        myViewHolder.meanings.setText(mergedMeaning.getMeaning());

        myViewHolder.hasPronunciations = mergedMeaning.hasPronunciation();

        if (myViewHolder.hasPronunciations) {
            myViewHolder.pronunciations.setVisibility(shouldHideHints ? View.GONE : View.VISIBLE);
            myViewHolder.pronunciations.setText("[" + mergedMeaning.getPronunciation() + "]");
        } else {
            myViewHolder.pronunciations.setVisibility(View.GONE);
        }

        myViewHolder.hasExamples = mergedMeaning.hasExample();

        if (myViewHolder.hasExamples) {
            myViewHolder.examples.setVisibility(shouldHideHints ? View.GONE : View.VISIBLE);
            myViewHolder.examples.setText(mergedMeaning.getExample());
        } else {
            myViewHolder.examples.setVisibility(View.GONE);
        }

        myViewHolder.wordAndPronunciations.setVisibility(shouldHideWord ? View.INVISIBLE : View.VISIBLE);
        myViewHolder.meaningsAndExamples.setVisibility(shouldHideMeanings ? View.INVISIBLE : View.VISIBLE);
    }

    public void setShouldHideWord(boolean shouldHideWord) {
        if (this.shouldHideWord != shouldHideWord) {
            this.shouldHideWord = shouldHideWord;

            updateViewHolders(viewHolder -> {
                final ViewHolder myViewHolder = (ViewHolder)viewHolder;

                myViewHolder.wordAndPronunciations.startAnimation(shouldHideWord ?
                        myViewHolder.wordAndPronunciationsCloseAnimation : myViewHolder.wordAndPronunciationsOpenAnimation);
                myViewHolder.wordAndPronunciations.setVisibility(shouldHideWord ? View.INVISIBLE : View.VISIBLE);
            });
        }
    }

    public void setShouldHideMeanings(boolean shouldHideMeanings) {
        if (this.shouldHideMeanings != shouldHideMeanings) {
            this.shouldHideMeanings = shouldHideMeanings;

            updateViewHolders(viewHolder -> {
                final ViewHolder myViewHolder = (ViewHolder)viewHolder;

                myViewHolder.meaningsAndExamples.startAnimation(shouldHideMeanings ?
                        myViewHolder.meaningsAndExamplesCloseAnimation : myViewHolder.meaningsAndExamplesOpenAnimation);
                myViewHolder.meaningsAndExamples.setVisibility(shouldHideMeanings ? View.INVISIBLE : View.VISIBLE);
            });
        }
    }

    public void setShouldHideHints(boolean shouldHideHints) {
        if (this.shouldHideHints != shouldHideHints) {
            this.shouldHideHints = shouldHideHints;

            updateViewHolders(viewHolder -> {
                final ViewHolder myViewHolder = (ViewHolder)viewHolder;
                boolean shouldNotify = false;

                if (myViewHolder.hasPronunciations) {
                    myViewHolder.pronunciations.startAnimation(shouldHideHints ?
                            myViewHolder.pronunciationsCloseAnimation : myViewHolder.pronunciationsOpenAnimation);
                    myViewHolder.pronunciations.setVisibility(shouldHideMeanings ? View.GONE : View.VISIBLE);

                    shouldNotify = true;
                }

                if (myViewHolder.hasExamples) {
                    myViewHolder.examples.startAnimation(shouldHideHints ?
                            myViewHolder.examplesCloseAnimation : myViewHolder.examplesOpenAnimation);
                    myViewHolder.examples.setVisibility(shouldHideMeanings ? View.GONE : View.VISIBLE);

                    shouldNotify = true;
                }

                if (shouldNotify) {
                    notifyItemChanged(myViewHolder.getAdapterPosition());
                }
            });
        }
    }
}
