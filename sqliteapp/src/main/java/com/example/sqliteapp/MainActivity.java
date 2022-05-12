package com.example.sqliteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

/* 1. сделать активити настройки, справка пустые
* 2. в списке слов продумать галочку/крестик или убрать
* 3.Сделать таймер при обучении вместо кнопки слежующее слово
* 4. сделать активити учить с вводом
* 5. сделать активити учить с тестом (определять одинаковые части речи по русскому переводу)
* 6. ??? сделать возможность добавления таблиц - подгрупп для слов
* 7. сделать navigation drawer со ссылками на: добавить, список, учить, настройки, справка
* 8. сделать чтобы где-то были подписаны языки, какой нэйтив какой таргет - ???библиотека с
*  выпадающим списком - ??? сохранять в sharedpreference - сначала просто вводим языки вручную,
* чтобы попробовать шаредпреференс
* 9. а если слово отредактировано во время обучения? нужно сделать чтобы был возврат обратно в
* обучение и там счетчик не обнулялся, т.е. фрагментом???
*
* В конце:
* 100. Сделать сохранение состояния
* 101. Сделать большие и маленькие экраны
* 102. Сделать ночной режим*/

public class MainActivity extends AppCompatActivity {

/*    EditText targetBox;
    EditText nativeBox;
    Button saveButton;
    Button studyButton;
    Button listButton;
    String t, n;
    DatabaseHelper sqlHelper;
    SQLiteDatabase db; */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_add_container_view, AddFragment.class, null)
                    .commit();
        }
    }
 /*       saveButton = findViewById(R.id.saveButton);
        studyButton = findViewById(R.id.studyButton);
        listButton = findViewById(R.id.listButton);
        targetBox = findViewById(R.id.targetBox);
        nativeBox = findViewById(R.id.nativeBox);

        sqlHelper = new DatabaseHelper(this);
        sqlHelper.create_db();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                t = targetBox.getText().toString();
                n = nativeBox.getText().toString();

           if (!StringUtils.isBlank(t) && !StringUtils.isBlank(n)) {
                   ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COLUMN_TARGET, t);
                    cv.put(DatabaseHelper.COLUMN_NATIVE, n);
                    db.insert(DatabaseHelper.TABLE, null, cv);
                     Toast.makeText(getApplicationContext(), "Успешно сохранено",
                              Toast.LENGTH_SHORT).show();
                targetBox.setText("");
                nativeBox.setText("");

               } else {
                   Toast.makeText(getApplicationContext(), "Пожалуйста, заполните обе строки",
                           Toast.LENGTH_SHORT).show();
               }
            }
        });
    }

 @Override
    public void onResume() {
        super.onResume();
        // открываем подключение
        db = sqlHelper.open();
    }


    public void study(View view) {
        Intent intent = new Intent(this, StudyActivity.class);
        startActivity(intent);
    }


    public void list(View view) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

 @Override
    public void onDestroy() {
      super.onDestroy();
      db.close();
  } */
}