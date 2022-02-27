package com.staticom.wordreminder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.staticom.wordreminder.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CheckableAdapter extends BaseAdapter {

    private static class ViewHolder {

        private int position = -1;

        private final TextView title;
        private final CheckBox checkBox;

        public ViewHolder(View view) {
            title = view.findViewById(R.id.title);
            checkBox = view.findViewById(R.id.checkBox);
        }
    }

    private final List<ViewHolder> viewHolders = new ArrayList<>();
    private final String defaultTitleText, titleTextFormat;
    private String titleText;

    private final String[] array;
    private boolean[] isSelected;

    public CheckableAdapter(String defaultTitleText, String titleTextFormat, String[] array) {
        this.defaultTitleText = defaultTitleText;
        this.titleTextFormat = titleTextFormat;
        this.titleText = defaultTitleText;

        this.array = array;
        isSelected = new boolean[array.length];
    }

    @Override
    public Object getItem(int position) {
        if (position == 0) return null;
        else return array[position - 1];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return array.length + 1;
    }

    private void updateTitleText() {
        final List<String> selectedTags = new ArrayList<>();

        for (int i = 0; i < isSelected.length; ++i) {
            if (isSelected[i]) {
                selectedTags.add(array[i]);
            }
        }

        if (selectedTags.isEmpty()) {
            titleText = defaultTitleText;
        } else {
            titleText = String.format(titleTextFormat, String.join(", ", selectedTags));
        }

        for (final ViewHolder vh : viewHolders) {
            if (vh.position == 0) {
                vh.title.setText(titleText);
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            final Context context = parent.getContext();
            final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.item_checkable, parent, false);
            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);

            viewHolders.add(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.position = position;

        if (position == 0) {
            viewHolder.title.setVisibility(View.VISIBLE);
            viewHolder.title.setText(titleText);

            viewHolder.checkBox.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.title.setVisibility(View.GONE);

            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setText(array[position - 1]);
            viewHolder.checkBox.setOnCheckedChangeListener(null);

            viewHolder.checkBox.setChecked(isSelected[position - 1]);
            viewHolder.checkBox.setOnCheckedChangeListener((view, isChecked) -> {
                isSelected[position - 1] = isChecked;

                updateTitleText();
            });
        }

        return convertView;
    }

    public boolean[] getIsSelected() {
        return Arrays.copyOf(isSelected, isSelected.length);
    }

    public void setIsSelected(boolean[] isSelected) {
        this.isSelected = isSelected;

        updateTitleText();
    }

    public void reset() {
        Arrays.fill(isSelected, false);

        updateTitleText();
    }
}