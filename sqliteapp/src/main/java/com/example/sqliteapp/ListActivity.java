package com.example.sqliteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class ListActivity extends AppCompatActivity {

    ListView wordList;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        wordList = findViewById(R.id.list);
        wordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        databaseHelper = new DatabaseHelper(getApplicationContext());
        // создаем базу данных
        databaseHelper.create_db();
    }
    @Override
    public void onResume() {
        super.onResume();
        // открываем подключение
        db = databaseHelper.open();
        //получаем данные из бд в виде курсора
        wordsCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
       /* String[] headers = new String[]{DatabaseHelper.COLUMN_TARGET, DatabaseHelper.COLUMN_NATIVE,
                DatabaseHelper.COLUMN_STUDY};*/
        // создаем адаптер, передаем в него курсор
        WordsAdapter wordsAdapter = new WordsAdapter(this, wordsCursor);
        wordList.setAdapter(wordsAdapter);
    }

    public void add(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void study(View view) {
        Intent intent = new Intent(this, StudyActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение и курсор
        db.close();
        wordsCursor.close();
    }
}