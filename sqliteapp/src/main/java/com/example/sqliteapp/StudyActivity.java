/* Активити для запоминания слов по принципу flashcard */

package com.example.sqliteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.example.sqliteapp.databinding.ActivityStudyBinding;
import java.util.ArrayList;
import java.util.Random;

public class StudyActivity extends AppCompatActivity {

    private ActivityStudyBinding binding;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    int linesCount, currentCount = 0;
    boolean isReversed, isShown, isExcluded = false;
    ArrayList<Integer> excludedList = new ArrayList<Integer>(); //коллекция для хранения айди исключенных
    //слов, при исключении одновременно удаляются из бд и записываются в эту коллекцию
    ArrayList<Integer> shownList = new ArrayList<Integer>(); //коллекция для хранения уже показанных слов
    Random r = new Random(); //объект для генерации рандомных чисел


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudyBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        databaseHelper = new DatabaseHelper(getApplicationContext());
        // открываем подключение
        db = databaseHelper.open();
        //получаем данные из таблицы бд, только те строки, которые не исключены из обучения,
        wordsCursor = db.rawQuery(
                "select * from " + DatabaseHelper.TABLE + " WHERE study = 1", null);


        //проверяем, чтобы в бд есть слова, иначе скрываем все view, кроме studyImpossible
        if (wordsCursor.getCount() == 0) {

            binding.studyImpossible.setVisibility(View.VISIBLE);
            binding.btnNext.setVisibility(View.GONE);
            binding. btnShow.setVisibility(View.GONE);
            binding.fieldTop.setVisibility(View.GONE);
            binding.counter.setVisibility(View.GONE);

        } else {
            //если в бд есть слова, то запускаем ОСНОВНОЙ ФУНКЦИОНАЛ ПРОГРАММЫ
            linesCount = wordsCursor.getCount();

            showFirstWord();

            binding. btnShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //показываем перевод слова в поле fieldBottom
                    if (isReversed)  binding.fieldBottom.setText(wordsCursor.getString(2));
                    else  binding.fieldBottom.setText(wordsCursor.getString(1));
                }
            });

            // по нажатию кнопки получаем следующую строку
            binding.btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // переходим на случайную строку
                    do {
                        wordsCursor.moveToPosition(r.nextInt(linesCount));
                        checkExclusion();
                        checkShown();
                    } while (isExcluded
                           || isShown
                    );

                    //выводим полученное слово
                    if (isReversed)  binding.fieldTop.setText(wordsCursor.getString(1));
                    else  binding.fieldTop.setText(wordsCursor.getString(2));
                    currentCount++;
                    binding.fieldBottom.setText("");
                    binding.counter.setText(getString(R.string.count, currentCount, linesCount));
                    shownList.add(wordsCursor.getInt(0));
                    //если все слова в базе уже показаны, то
                    if (shownList.size() == linesCount) {
                        binding.btnNext.setVisibility(View.GONE);
                        binding.btnRestart.setVisibility(View.VISIBLE);
                    }
                }
            });
            binding.btnRestart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shownList.clear();
                    binding.fieldBottom.setText("");
                    binding.btnNext.setVisibility(View.VISIBLE);
                    binding.btnRestart.setVisibility(View.GONE);
                    showFirstWord();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.study_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.exclude:
                exclude();
                return true;
            case R.id.swap:
                swapLangs();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

     public void checkExclusion() {
        if (excludedList.isEmpty()) {
            isExcluded = false;
        } else {
            for (int i = 0, excludedSize = excludedList.size(); i < excludedSize; i++) {
                Integer integer = excludedList.get(i);
                if (integer == wordsCursor.getInt(0)) {
                    isExcluded = true;
                    break;
                }
            }
        }
     }

    public void checkShown() {
        isShown = false;
        for (int i = 0, shownSize = shownList.size(); i < shownSize; i++) {
            Integer integer = shownList.get(i);
            if (integer == wordsCursor.getInt(0)) {
                isShown = true;
                break;
            }
        }
    }

    public void exclude() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_STUDY, 0);
        Toast.makeText(this, "Успешно исключено",Toast.LENGTH_LONG).show();
        db.update(DatabaseHelper.TABLE, cv, DatabaseHelper.COLUMN_ID + "=" + wordsCursor.getInt(0), null);
        //записываем айди удаленных слов в коллекцию
        excludedList.add(wordsCursor.getInt(0));
        //уменьшаем счетчики на одно слово
        currentCount--;
        linesCount--;
    }

    public void showFirstWord() {
        //устанавливаем курсор и генерируем первое значение
        wordsCursor.moveToFirst();
        wordsCursor.moveToPosition(r.nextInt(linesCount));

        //устаналиваем первое значение и счетчик
        if (isReversed)  binding.fieldTop.setText(wordsCursor.getString(1));
        else  binding.fieldTop.setText(wordsCursor.getString(2));
        shownList.add(wordsCursor.getInt(0));
        currentCount = 1;
        binding.counter.setText(getString(R.string.count, currentCount, linesCount));
    }

    public void swapLangs() {
        isReversed = !isReversed;
        shownList.clear();
        showFirstWord();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение и курсор
        db.close();
        wordsCursor.close();
    }
}