package com.staticom.wordreminder.adapter;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Vocabulary;
import com.staticom.wordreminder.core.Word;

import java.util.stream.Collectors;

public class DetailedWordsAdapter extends SelectableAdapter {

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final ConstraintLayout header;
        private final Animation headerOpenAnimation, headerCloseAnimation;

        private final TextView word;
        private boolean hasPronunciations;
        private final TextView pronunciations;

        private final ConstraintLayout body;
        private final Animation bodyOpenAnimation, bodyCloseAnimation;

        private final TextView meanings;
        private boolean hasExamples, hasRelations;
        private final TextView examples, relations;

        public ViewHolder(Context applicationContext, View view) {
            super(view);

            header = view.findViewById(R.id.header);
            headerOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_open);
            headerCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_close);

            word = view.findViewById(R.id.word);
            pronunciations = view.findViewById(R.id.pronunciations);

            body = view.findViewById(R.id.body);
            bodyOpenAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_open);
            bodyCloseAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.tv_close);

            meanings = view.findViewById(R.id.meanings);
            examples = view.findViewById(R.id.examples);
            relations = view.findViewById(R.id.relations);
        }
    }

    private Vocabulary vocabulary;

    private boolean hideWord = false, hideMeanings = false, hideHints = false;

    public DetailedWordsAdapter(Vocabulary vocabulary) {
        super(R.layout.item_detailed_word);

        this.vocabulary = vocabulary;
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;

        notifyDataSetChanged();
    }

    public void setHideWord(boolean hideWord) {
        if (this.hideWord != hideWord) {
            this.hideWord = hideWord;

            updateViewHolders(viewHolder -> {
                final ViewHolder myViewHolder = (ViewHolder)viewHolder;

                myViewHolder.header.startAnimation(hideWord ?
                        myViewHolder.headerCloseAnimation : myViewHolder.headerOpenAnimation);
                myViewHolder.header.setVisibility(hideWord ? View.INVISIBLE : View.VISIBLE);
            });
        }
    }

    public void setHideMeanings(boolean hideMeanings) {
        if (this.hideMeanings != hideMeanings) {
            this.hideMeanings = hideMeanings;

            updateViewHolders(viewHolder -> {
                final ViewHolder myViewHolder = (ViewHolder)viewHolder;

                myViewHolder.body.startAnimation(hideMeanings ?
                        myViewHolder.bodyCloseAnimation : myViewHolder.bodyOpenAnimation);
                myViewHolder.body.setVisibility(hideMeanings ? View.INVISIBLE : View.VISIBLE);
            });
        }
    }

    public void setHideHints(boolean hideHints) {
        if (this.hideHints != hideHints) {
            this.hideHints = hideHints;

            updateViewHolders(viewHolder -> {
                final ViewHolder myViewHolder = (ViewHolder)viewHolder;

                if (myViewHolder.hasPronunciations) {
                    myViewHolder.pronunciations.setVisibility(hideMeanings ? View.GONE : View.VISIBLE);
                }

                if (myViewHolder.hasExamples) {
                    myViewHolder.examples.setVisibility(hideMeanings ? View.GONE : View.VISIBLE);
                }

                if (myViewHolder.hasRelations) {
                    myViewHolder.relations.setVisibility(hideMeanings ? View.GONE : View.VISIBLE);
                }

                if (myViewHolder.hasPronunciations || myViewHolder.hasExamples || myViewHolder.hasRelations) {
                    notifyItemChanged(myViewHolder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return vocabulary.getWords().size();
    }

    @Override
    protected SelectableAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view.getContext().getApplicationContext(), view);
    }

    @Override
    public void onBindViewHolder(SelectableAdapter.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        final ViewHolder myViewHolder = (ViewHolder)viewHolder;

        myViewHolder.header.clearAnimation();
        myViewHolder.body.clearAnimation();

        final Word word = vocabulary.getWord(position);
        final Meaning mergedMeaning = word.mergeMeanings(", ", ", ", "\n");

        myViewHolder.word.setText(word.getWord());
        myViewHolder.meanings.setText(mergedMeaning.getMeaning());

        myViewHolder.hasPronunciations = mergedMeaning.hasPronunciation();

        if (myViewHolder.hasPronunciations) {
            myViewHolder.pronunciations.setVisibility(hideHints ? View.GONE : View.VISIBLE);
            myViewHolder.pronunciations.setText("[" + mergedMeaning.getPronunciation() + "]");
        } else {
            myViewHolder.pronunciations.setVisibility(View.GONE);
        }

        myViewHolder.hasExamples = mergedMeaning.hasExample();

        if (myViewHolder.hasExamples) {
            myViewHolder.examples.setVisibility(hideHints ? View.GONE : View.VISIBLE);
            myViewHolder.examples.setText(mergedMeaning.getExample());
        } else {
            myViewHolder.examples.setVisibility(View.GONE);
        }

        myViewHolder.hasRelations = word.hasRelations();

        if (myViewHolder.hasRelations) {
            myViewHolder.relations.setVisibility(hideHints ? View.GONE : View.VISIBLE);
            myViewHolder.relations.setText(HtmlCompat.fromHtml(
                    word.getRelations().stream().map(relation -> String.format(
                            viewHolder.itemView.getContext().getString(R.string.detailed_word_adapter_relation),
                            relation.getWord().getWord(),
                            relation.getRelation())).collect(Collectors.joining("<br>")),
                    HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            myViewHolder.relations.setVisibility(View.GONE);
        }

        myViewHolder.header.setVisibility(hideWord ? View.INVISIBLE : View.VISIBLE);
        myViewHolder.body.setVisibility(hideMeanings ? View.INVISIBLE : View.VISIBLE);
    }

    public Word getSelectedWord() {
        return vocabulary.getWord(getSelectedIndex());
    }
}