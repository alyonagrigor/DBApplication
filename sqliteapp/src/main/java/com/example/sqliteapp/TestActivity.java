package com.example.sqliteapp;

/**
 * 1. сделать функцию поменять языки местами внутри данной активити
 * 2.
 * 3. Возможно, сделать переход к редактированию слова в верхнем меню. Не получается сделать
 * фрагментами testactivity, studyactivity т.к. у фрагментов некорректно работает optionsmenu
 * 4. Возможно, сделать ротацию так, чтобы слова не повторялись, тогда будут иметь смысл счетчики
 * 5. Возможно, сделать временную задержку и задавать ее в настройках
 * */

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
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class TestActivity extends AppCompatActivity {

    TextView fieldTop, counterBox;
    Button btnNext;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    int linesCount, currentCount, rightWordPosition = 0;
    ArrayList<String> wrongWords = new ArrayList<>(); //список для хранения неправильных вариантов ответа в тесте
    ArrayList<Integer> excluded = new ArrayList<Integer>(); //коллекция для хранения айди исключенных
    //слов, при исключении одновременно удаляются из бд и записываются в эту коллекцию
    Iterator<Integer> iter = excluded.iterator(); //итератор для коллекции
    boolean isExcluded = false;
    Random r = new Random(); //объект для генерации рандомных чисел
    RadioGroup radGrp;
    RadioButton rBtn1, rBtn2, rBtn3, rBtn4;
    String rightWord, rightWordEnding, curWord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        btnNext = findViewById(R.id.btnNext);
        radGrp = findViewById(R.id.radioGroup);
        fieldTop = findViewById(R.id.fieldTop);
        counterBox = findViewById(R.id.counter);
        rBtn1 = findViewById(R.id.rBtn1);
        rBtn2 = findViewById(R.id.rBtn2);
        rBtn3 = findViewById(R.id.rBtn3);
        rBtn4 = findViewById(R.id.rBtn4);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Test");
        }

        databaseHelper = new DatabaseHelper(getApplicationContext());
        // открываем подключение
        db = databaseHelper.open();
        //получаем данные из таблицы бд, только те строки, которые не исключены из обучения,
        wordsCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE + " WHERE study = 1", null);
        linesCount = wordsCursor.getCount();

        //устанавливаем курсор и устанавливаем на рандомную строку
        wordsCursor.moveToFirst();
        wordsCursor.moveToPosition(r.nextInt(linesCount));

        //устаналиваем значение на родном языке и счетчик
        fieldTop.setText(wordsCursor.getString(2));
        currentCount = 1;
        counterBox.setText(currentCount + " / " + linesCount);
        radGrp.clearCheck();

        //выводим варианты ответа
        showOptions();

// ***********************************************!!!НАЧАЛО СЛУШАТЕЛЕЙ!!!**************************

        radGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radGrp, int id) {

                if (id == R.id.rBtn1) {
                    if (rightWordPosition == 1) {
                    //    showToastRight();
                    btnNext.setEnabled(true);
                    }
                    else showToastWrong();

                } if (id == R.id.rBtn2) {
                    if (rightWordPosition == 2)  {
                    //    showToastRight();
                        btnNext.setEnabled(true);
                    }
                    else showToastWrong();

                } if (id == R.id.rBtn3) {
                    if (rightWordPosition == 3)  {
                    //    showToastRight();
                        btnNext.setEnabled(true);
                    }
                    else showToastWrong();

                } if (id == R.id.rBtn4) {
                    if (rightWordPosition == 4)  {
                    //    showToastRight();
                        btnNext.setEnabled(true);
                    }
                    else showToastWrong();
                }

            }});

        //временная задержка
          /*      try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }*/


        // по нажатию кнопки получаем следующую строку
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // переходим на случайную строку
                do {
                    wordsCursor.moveToPosition(r.nextInt(linesCount));
                    //проверяем, не было ли слово исключено в процессе работы с текущей активити
                    checkExclusion();
                } while (isExcluded);

                //выводим полученное слово
                fieldTop.setText(wordsCursor.getString(2));
                currentCount++;
                counterBox.setText(currentCount + " / " + linesCount);
                radGrp.clearCheck();
                wrongWords.clear();

                //выводим варианты ответа
                showOptions();
                //деактивируем кнопку "следующее слово"
                btnNext.setEnabled(false);
            }
        });
    }

// ***********************************************!!!КОНЕЦ МЕТОДА MAIN!!!**************************

    // метод, который выводит правильный и похожие варианты ответа в радиогруп
    public void showOptions () {

        //записываем правильный ответ в строку
        rightWord = wordsCursor.getString(1);

        //подбираем подходящие варианты неправильных ответов
        rightWordEnding = getThreeLastChars(rightWord); //получаем окончание
        //правильного ответа в отдельную строку, последние 3 буквы, например, soledad - -dad

        // перебираем строки в курсоре циклом и записываем подходящие варианты в список
        for (int i = 0; i < linesCount; i++) {
            wordsCursor.moveToPosition(i);
            curWord = wordsCursor.getString(1);
            if (getThreeLastChars(curWord).equals(rightWordEnding)
                    && !curWord.equals(rightWord)
                    && !wrongWords.contains(curWord)
                    ){
                wrongWords.add(curWord);
            }
            if (wrongWords.size() == 3) {
                break;
            }
        }

        //если получилось меньше 3 значений, то ищем слова, с которыми совпадают 2 буквы в конце
        if (wrongWords.size() < 3) {
            rightWordEnding = getTwoLastChars(rightWord);
            for (int i = 0; i < linesCount; i++) {
                wordsCursor.moveToPosition(i);
                curWord = wordsCursor.getString(1);
                if (getTwoLastChars(curWord).equals(rightWordEnding)
                        && !curWord.equals(rightWord)
                        && !wrongWords.contains(curWord)
                ) {
                    wrongWords.add(curWord);
                }
                if (wrongWords.size() == 3) {
                    break;
                }
            }
        }
//если получилось меньше 3 значений, то ищем слова, с которыми совпадают 1 буква в конце
        if (wrongWords.size()<3) {
            rightWordEnding = getOneLastChar(rightWord);
            for (int i = 0; i < linesCount; i++) {
                wordsCursor.moveToPosition(i);
                curWord = wordsCursor.getString(1);
                if (getOneLastChar(curWord).equals(rightWordEnding)
                        && !curWord.equals(rightWord)
                        && !wrongWords.contains(curWord)
                ) {
                    wrongWords.add(curWord);
                }
                if (wrongWords.size() == 3) {
                    break;
                }
            }
        }

        //если вариантов все равно не хватило, берем любое рандомное значение
        if (wrongWords.size()<3) {
            do {
                wordsCursor.moveToPosition(r.nextInt(linesCount));
            //    if (!wrongWords.contains(wordsCursor.getString(1)))
                wrongWords.add(wordsCursor.getString(1));
            } while (wrongWords.size()==3);
        }

//рандомно выбираем и записываем позицию для правильного ответа
        rightWordPosition = r.nextInt(4) + 1;

        //выводим полученные ответы в радиогруп
        switch (rightWordPosition) {
            case 1:
                rBtn1.setText(rightWord);
                rBtn2.setText(wrongWords.get(0));
                rBtn3.setText(wrongWords.get(1));
                rBtn4.setText(wrongWords.get(2));
                break;
            case 2:
                rBtn1.setText(wrongWords.get(0));
                rBtn2.setText(rightWord);
                rBtn3.setText(wrongWords.get(1));
                rBtn4.setText(wrongWords.get(2));
                break;
            case 3:
                rBtn1.setText(wrongWords.get(0));
                rBtn2.setText(wrongWords.get(1));
                rBtn3.setText(rightWord);
                rBtn4.setText(wrongWords.get(2));
                break;
            case 4:
                rBtn1.setText(wrongWords.get(0));
                rBtn2.setText(wrongWords.get(1));
                rBtn3.setText(wrongWords.get(2));
                rBtn4.setText(rightWord);
                break;
        }
    }

    //подсказки правильный/неправильный ответ
    public void showToastWrong () {
        Toast.makeText(getApplicationContext(), "Неправильный ответ, попробуйте еще раз",
                Toast.LENGTH_SHORT).show();
    }

    public void showToastRight () {
        Toast.makeText(getApplicationContext(), "Правильный ответ", Toast.LENGTH_SHORT).show();
    }


    //методы для верхнего меню---------------------------------------------------------------------
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
        }
        return super.onOptionsItemSelected(item);
    }

   public void exclude() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_STUDY, 0);
        Toast.makeText(this, "Успешно исключено",Toast.LENGTH_LONG).show();
        db.update(DatabaseHelper.TABLE, cv,
                DatabaseHelper.COLUMN_ID + "=" + wordsCursor.getInt(0), null);
        //записываем айди удаленных слов в коллекцию
        excluded.add(wordsCursor.getInt(0));
        //уменьшаем счетчики на одно слово
        currentCount--;
        linesCount--;
   }

    //***конец методов верхнего меню---------------------------------------------------------------

   public void checkExclusion() {

        if (excluded.isEmpty()) {
            isExcluded = false;
        } else {
            while (iter.hasNext()) {
                if (iter.next() == wordsCursor.getInt(0)) {
                    isExcluded = true;
                    break;
                }
            }
        }
   }

    public String getThreeLastChars(String str) { return str.substring(str.length() - 3); }

    public String getTwoLastChars(String str) { return str.substring(str.length() - 2); }

    public String getOneLastChar (String str) { return str.substring(str.length() - 1); }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение и курсор
        db.close();
        wordsCursor.close();
    }
}