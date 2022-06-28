package com.example.sqliteapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WordsAdapter extends CursorAdapter {

    private LayoutInflater mInflater;

public WordsAdapter(Context context, Cursor cursor){
            super(context, cursor, 1);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.list_item, parent,false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            View listItem = view.findViewById(R.id.listItem);
            TextView targetText = view.findViewById(R.id.targetCell);
            TextView nativeText = view.findViewById(R.id.nativeCell);
            ImageView flagCell = view.findViewById(R.id.flagCell);

            targetText.setText(cursor.getString(1));
            nativeText.setText(cursor.getString(2));

            if (cursor.getInt(3) == 1) { flagCell.setImageResource(R.drawable.ic_yes); }
            else { flagCell.setImageResource(R.drawable.ic_no); }

            //программно задаем фон (белый или серый в зависимости от опции "Включить в обучение")
            // и границы для строк таблицы View listItem
            GradientDrawable backgroundGrey = new GradientDrawable();
            GradientDrawable backgroundWhite = new GradientDrawable();
            backgroundGrey.setColor(0xFFEEEEEE);
            backgroundGrey.setStroke(1, 0xFF84FFEE);
            backgroundWhite.setColor(0xFFFFFFFF);
            backgroundWhite.setStroke(1, 0xFF84FFEE);

            int study = cursor.getInt(3);
            if (study == 1) { listItem.setBackground(backgroundWhite); }
            else { listItem.setBackground(backgroundGrey); }
        }
}
