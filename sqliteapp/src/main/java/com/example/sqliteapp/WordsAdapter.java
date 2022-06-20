package com.example.sqliteapp;

import android.content.Context;
import android.database.Cursor;
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
            TextView targetText = view.findViewById(R.id.targetCell); // инициализировали textView
            String targetString = cursor.getString(1);// достали строку в переменную
            targetText.setText(targetString);// присвоили значение

            TextView nativeText = view.findViewById(R.id.nativeCell);
            String nativeString = cursor.getString(2);
            nativeText.setText(nativeString);

            ImageView flagCell = view.findViewById(R.id.flagCell);
            if (cursor.getInt(3) == 1) { flagCell.setImageResource(R.drawable.yes); }
            else { flagCell.setImageResource(R.drawable.no); }

            int study = cursor.getInt(3); //нашли айди элемента с БД
            //Log.e("GAdapter", "bindView: idGroup = " + idGroup);
            if (study == 0){ // если id = 0, меняем цвета текстов на те, что есть в файле colors
                //Log.e("GAdapter", "bindView: idGroup == 11\nзакрашиваем строчки");
                targetText.setBackgroundColor(context.getResources().getColor(R.color.grey));
                nativeText.setBackgroundColor(context.getResources().getColor(R.color.grey));
                flagCell.setBackgroundColor(context.getResources().getColor(R.color.grey));
            } else { // если не равен 0 (как раз этот блок не даст цветам меняться при прокрутке)
                targetText.setBackgroundColor(context.getResources().getColor(R.color.white));
                nativeText.setBackgroundColor(context.getResources().getColor(R.color.white));
                flagCell.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
        }
}
