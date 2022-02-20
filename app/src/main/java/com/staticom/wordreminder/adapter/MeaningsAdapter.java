package com.staticom.wordreminder.adapter;

import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.text.HtmlCompat;

import com.staticom.wordreminder.R;
import com.staticom.wordreminder.core.Meaning;
import com.staticom.wordreminder.core.Word;

import java.util.ArrayList;
import java.util.List;

public class MeaningsAdapter extends SelectableAdapter {

    private class ViewHolder extends SelectableAdapter.ViewHolder {

        private final ConstraintLayout rootLayout;

        private final TextView meaning;
        private final TextView pronunciation;
        private final TextView example;

        public ViewHolder(View view) {
            super(view);

            rootLayout = view.findViewById(R.id.rootLayout);

            meaning = view.findViewById(R.id.meaning);
            pronunciation = view.findViewById(R.id.pronunciation);
            example = view.findViewById(R.id.example);

            onHolderDeactivated(false);
        }

        private int getPixelsByDIP(int dip) {
            return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                    itemView.getResources().getDisplayMetrics()) + 0.5f);
        }

        private void setOptionalViewsVisibility(boolean pronunciationVisibility, boolean exampleVisibility) {
            pronunciation.setVisibility(pronunciationVisibility ? View.VISIBLE : View.GONE);
            example.setVisibility(exampleVisibility ? View.VISIBLE : View.GONE);

            final List<Integer> views = new ArrayList<>();

            views.add(R.id.meaning);
            views.add(pronunciationVisibility ? R.id.pronunciation : 0);
            views.add(exampleVisibility ? R.id.example : 0);
            views.add(R.id.dummy);

            views.removeIf(layoutId -> layoutId == 0);

            final ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootLayout);

            for (int i = 0; i < views.size() - 1; ++i) {
                final int current = views.get(i);
                final int next = views.get(i + 1);

                constraintSet.connect(current, ConstraintSet.BOTTOM, next, ConstraintSet.TOP, getPixelsByDIP(4));
                constraintSet.connect(next, ConstraintSet.TOP, current, ConstraintSet.BOTTOM, 0);
            }

            constraintSet.applyTo(rootLayout);
        }

        @Override
        public void onHolderActivated(boolean isBindMode) {
            final Meaning meaning = word.getMeaning(getAdapterPosition());

            setOptionalViewsVisibility(meaning.hasPronunciation(), meaning.hasExample());
        }

        @Override
        public void onHolderDeactivated(boolean isBindMode) {
            setOptionalViewsVisibility(false, false);
        }
    }

    private final Word word;

    public MeaningsAdapter(Word word) {
        super(R.layout.item_meaning);

        this.word = word;
    }

    @Override
    public int getItemCount() {
        return word.getMeanings().size();
    }

    @Override
    protected SelectableAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SelectableAdapter.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);

        final ViewHolder myViewHolder = (ViewHolder)viewHolder;
        final Meaning meaning = word.getMeaning(position);

        myViewHolder.meaning.setText(meaning.getMeaning());
        myViewHolder.pronunciation.setText(HtmlCompat.fromHtml(
                String.format(
                        viewHolder.itemView.getContext().getString(R.string.meanings_adapter_pronunciation),
                        meaning.getPronunciation()), HtmlCompat.FROM_HTML_MODE_LEGACY));
        myViewHolder.example.setText(HtmlCompat.fromHtml(
                String.format(
                        viewHolder.itemView.getContext().getString(R.string.meanings_adapter_example),
                        meaning.getExample()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
}
