package com.example.sqliteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;


import org.apache.commons.lang3.StringUtils;

public class EditActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener {

    EditText targetBox;
    EditText nativeBox;
    Button delButton;
    Button saveButton;
    String t, n, targetLangWord;
    Switch toggleBtn;
    int checkedDigit;
    DatabaseHelper sqlHelper;
    SQLiteDatabase db;
    Cursor editCursor;
    long wordId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

            targetBox = findViewById(R.id.targetBox);
            nativeBox = findViewById(R.id.nativeBox);
            delButton = findViewById(R.id.deleteButton);
            saveButton = findViewById(R.id.saveButton);
            toggleBtn =  findViewById(R.id.toggleBtn);
            toggleBtn.setOnCheckedChangeListener(this);

            sqlHelper = new DatabaseHelper(this);
            db = sqlHelper.open();

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                wordId = extras.getLong("id");
            }
            // если 0, то добавление
            if (wordId > 0) {
                // получаем элемент по id из бд
                editCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE + " where " +
                        DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(wordId)});
                editCursor.moveToFirst();
                targetLangWord = editCursor.getString(1);
                targetBox.setText(targetLangWord);
                nativeBox.setText(editCursor.getString(2));
                //получаем и выставляем булево значение учить/не учить
               if (editCursor.getInt(3) == 1) {
                   toggleBtn.setChecked(true);
                   checkedDigit = 1;
               } else if (editCursor.getInt(3) == 0) {
                    toggleBtn.setChecked(false);
                   checkedDigit = 0;
               } else {
                   //если будет null или еще какая-то ошибка, то устанавливаем значение true и
                   //выводим toast
                   toggleBtn.setChecked(true);
                   checkedDigit = 1;
                   Toast.makeText(this, "Пожалуйста, укажите, нужно ли включать это слово в обучение",
                           Toast.LENGTH_LONG).show();
               }
                editCursor.close();
            } else {
                // скрываем кнопку удаления
                delButton.setVisibility(View.GONE);
            }

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialogFragment dialog = new CustomDialogFragment();
                Bundle args = new Bundle();
                args.putString("word", targetLangWord);
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "custom");
            }
        });
        }

        public void save(View view){
            t = targetBox.getText().toString();
            n = nativeBox.getText().toString();

            if (!StringUtils.isBlank(t) && !StringUtils.isBlank(n)) {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COLUMN_TARGET, targetBox.getText().toString());
                cv.put(DatabaseHelper.COLUMN_NATIVE, nativeBox.getText().toString());
                cv.put(DatabaseHelper.COLUMN_STUDY, checkedDigit);

            if (wordId > 0) {
                db.update(DatabaseHelper.TABLE, cv, DatabaseHelper.COLUMN_ID + "=" + wordId, null);
            } else {
                db.insert(DatabaseHelper.TABLE, null, cv);
            }
            goHome();

            } else {
                Toast.makeText(getApplicationContext(), "Пожалуйста, заполните обе строки",
                        Toast.LENGTH_SHORT).show();
            }
        }

        public void delete(View view){
            db.delete(DatabaseHelper.TABLE, "_id = ?", new String[]{String.valueOf(wordId)});
            goHome();
        }
        private void goHome() {
            // закрываем подключение
            db.close();
            // переход к списку слов ??? добавить переход обратно в учить??? если пришел из учить
            Intent intent = new Intent(this, ListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked)
            checkedDigit = 1;
        else
            checkedDigit = 0;
    }
}