package com.example.dbapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button addButton;
    EditText targetBox, nativeBox;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    ListView wordsList;
  //  TextView header;
    SimpleCursorAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.addButton);
        targetBox = findViewById(R.id.targetBox);
        nativeBox = findViewById(R.id.nativeBox);
        wordsList = findViewById(R.id.list2);
    //    header= findViewById(R.id.header);

        databaseHelper = new DatabaseHelper(getApplicationContext());
        // подключаемся к БД
        databaseHelper.create_db();
    }

   @Override
    public void onResume() {
        super.onResume();
        // открываем подключение
        db = databaseHelper.open();

       //получаем данные из бд в виде курсора
       userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
       // определяем, какие столбцы из курсора будут выводиться в ListView
       String[] headers = new String[]{DatabaseHelper.COLUMN_TARGET, DatabaseHelper.COLUMN_STUDY};
       // создаем адаптер, передаем в него курсор
       userAdapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item,
               userCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);
    //   header.setText("Найдено элементов: " + userCursor.getCount());
       wordsList.setAdapter(userAdapter);
    }


    // по нажатию на кнопку запускаем UserActivity для добавления данных
   public void add(View view) {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COLUMN_TARGET, targetBox.getText().toString());
            cv.put(DatabaseHelper.COLUMN_NATIVE, nativeBox.getText().toString());
            cv.put(DatabaseHelper.COLUMN_STUDY, 1);
            db.insert(DatabaseHelper.TABLE, null, cv);

        Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
    }

 /*   public void browse(View view) {
        Intent intent = new Intent(this, BrowseActivity.class);
        startActivity(intent);

    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение
        db.close();
        userCursor.close();
    }
}