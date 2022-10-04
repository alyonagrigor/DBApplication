/* Активити с фунцкионалом тестов */

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
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.sqliteapp.databinding.ActivityTestBinding;
import java.util.ArrayList;
import java.util.Random;

public class TestActivity extends AppCompatActivity {

    private ActivityTestBinding binding;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    int linesCount, currentCount, rightWordPosition = 0;
    ArrayList<String> wrongWords = new ArrayList<>(); //список для хранения неправильных вариантов ответа в тесте
    ArrayList<Integer> excludedList = new ArrayList<Integer>(); //коллекция для хранения айди исключенных
    //слов, при исключении одновременно удаляются из бд и записываются в эту коллекцию
    ArrayList<Integer> shownList = new ArrayList<Integer>(); //коллекция для хранения уже показанных слов
    boolean isReversed, isExcluded, isShown = false;
    Random r = new Random(); //объект для генерации рандомных чисел

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
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

        //проверяем, чтобы в бд было более 4 слов, иначе скрываем все view, кроме testImpossible
        if (wordsCursor.getCount() < 4) {

            binding.testImpossible.setVisibility(View.VISIBLE);
            binding.btnNext.setVisibility(View.GONE);
            binding.radioGroup.setVisibility(View.GONE);
            binding.fieldTop.setVisibility(View.GONE);
            binding.counter.setVisibility(View.GONE);

        } else {
            //если в бд более 4 слов, то запускаем ОСНОВНОЙ ФУНКЦИОНАЛ ПРОГРАММЫ
            linesCount = wordsCursor.getCount();
            showFirstWord();


            binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radGrp, int id) {

                    if (id == R.id.rBtn1) {
                        if (rightWordPosition == 1) {
                            binding.btnNext.setEnabled(true);
                            binding.btnRestart.setEnabled(true);
                        } else showToastWrong();

                    }
                    if (id == R.id.rBtn2) {
                        if (rightWordPosition == 2) {
                            binding.btnNext.setEnabled(true);
                            binding.btnRestart.setEnabled(true);
                        } else showToastWrong();

                    }
                    if (id == R.id.rBtn3) {
                        if (rightWordPosition == 3) {
                            binding.btnNext.setEnabled(true);
                            binding.btnRestart.setEnabled(true);
                        } else showToastWrong();

                    }
                    if (id == R.id.rBtn4) {
                        if (rightWordPosition == 4) {
                            binding.btnNext.setEnabled(true);
                            binding.btnRestart.setEnabled(true);
                        } else showToastWrong();
                    }

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
                    } while (isExcluded || isShown);

                    //выводим полученное слово
                    if (isReversed)  binding.fieldTop.setText(wordsCursor.getString(1));
                    else  binding.fieldTop.setText(wordsCursor.getString(2));
                    currentCount++;
                    binding.counter.setText(currentCount + " / " + linesCount);
                    binding.radioGroup.clearCheck();
                    wrongWords.clear();
                    shownList.add(wordsCursor.getInt(0));
                    //если все слова в базе уже показаны, то
                    if (shownList.size() == linesCount) {
                        binding.btnNext.setVisibility(View.GONE);
                        binding.btnRestart.setVisibility(View.VISIBLE);
                    }

                    //выводим варианты ответа
                    showOptions();
                    //деактивируем кнопку "следующее слово"
                    binding.btnNext.setEnabled(false);
                    binding.btnRestart.setEnabled(false);
                }
            });

            binding.btnRestart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shownList.clear();
                    binding.btnNext.setVisibility(View.VISIBLE);
                    binding.btnRestart.setVisibility(View.GONE);
                    showFirstWord();
                }
            });
        }
    }

    // метод, который выводит правильный и похожие варианты ответа в радиогруп
    public void showOptions () {
        String rightWord, rightWordEnding, curWord;

        //записываем правильный ответ в строку
        if (isReversed) rightWord = wordsCursor.getString(2);
        else rightWord = wordsCursor.getString(1);

        //подбираем подходящие варианты неправильных ответов
        rightWordEnding = getThreeLastChars(rightWord); //получаем окончание
        //правильного ответа в отдельную строку, последние 3 буквы, например, soledad - -dad

        //если смена языка включена
        if (isReversed) {
            // перебираем строки в курсоре циклом и записываем подходящие варианты в список
            for (int i = 0; i < linesCount; i++) {
                wordsCursor.moveToPosition(i);
                curWord = wordsCursor.getString(2);
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
                    curWord = wordsCursor.getString(2);
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
            if (wrongWords.size() < 3) {
                rightWordEnding = getOneLastChar(rightWord);
                for (int i = 0; i < linesCount; i++) {
                    wordsCursor.moveToPosition(i);
                    curWord = wordsCursor.getString(2);
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
            if (wrongWords.size() < 3) {
                do {
                    wordsCursor.moveToPosition(r.nextInt(linesCount));
                    curWord = wordsCursor.getString(2);
                    if (!curWord.equals(rightWord)
                        && !wrongWords.contains(curWord)
                    ) {
                        wrongWords.add(curWord);
                    }
                //    wrongWords.add("Test");
                } while (wrongWords.size() < 3);
            }
        } else { //если смена языка выключена
            // перебираем строки в курсоре циклом и записываем подходящие варианты в список
            for (int i = 0; i < linesCount; i++) {
                wordsCursor.moveToPosition(i);
                curWord = wordsCursor.getString(1);
                if (getThreeLastChars(curWord).equals(rightWordEnding)
                        && !curWord.equals(rightWord)
                        && !wrongWords.contains(curWord)
                ) {
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
            if (wrongWords.size() < 3) {
                do {
                    wordsCursor.moveToPosition(r.nextInt(linesCount));
                    curWord = wordsCursor.getString(1);
                    if (!curWord.equals(rightWord)
                            && !wrongWords.contains(curWord)
                    ) {
                        wrongWords.add(curWord);
                    }
                } while (wrongWords.size() < 3);
            }
        }

        //рандомно выбираем и записываем позицию для правильного ответа
        rightWordPosition = r.nextInt(4) + 1;

        //выводим полученные ответы в радиогруп
        switch (rightWordPosition) {
            case 1:
                binding.rBtn1.setText(rightWord);
                binding.rBtn2.setText(wrongWords.get(0));
                binding.rBtn3.setText(wrongWords.get(1));
                binding.rBtn4.setText(wrongWords.get(2));
                break;
            case 2:
                binding.rBtn1.setText(wrongWords.get(0));
                binding.rBtn2.setText(rightWord);
                binding.rBtn3.setText(wrongWords.get(1));
                binding.rBtn4.setText(wrongWords.get(2));
                break;
            case 3:
                binding.rBtn1.setText(wrongWords.get(0));
                binding.rBtn2.setText(wrongWords.get(1));
                binding.rBtn3.setText(rightWord);
                binding.rBtn4.setText(wrongWords.get(2));
                break;
            case 4:
                binding.rBtn1.setText(wrongWords.get(0));
                binding.rBtn2.setText(wrongWords.get(1));
                binding.rBtn3.setText(wrongWords.get(2));
                binding.rBtn4.setText(rightWord);
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


    //методы для верхнего меню
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

    //методы для основного функционала
    public void exclude() {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_STUDY, 0);
        Toast.makeText(this, "Успешно исключено",Toast.LENGTH_LONG).show();
        db.update(DatabaseHelper.TABLE, cv,
                DatabaseHelper.COLUMN_ID + "=" + wordsCursor.getInt(0), null);
        //записываем айди удаленных слов в коллекцию
        excludedList.add(wordsCursor.getInt(0));
        //уменьшаем счетчики на одно слово
        currentCount--;
        linesCount--;
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

    public String getThreeLastChars(String str) { return str.substring(str.length() - 3); }

    public String getTwoLastChars(String str) { return str.substring(str.length() - 2); }

    public String getOneLastChar (String str) { return str.substring(str.length() - 1); }

    public void swapLangs() {
        isReversed = !isReversed;
        shownList.clear();
        showFirstWord();
    }

    public void showFirstWord() {
        //устанавливаем курсор и устанавливаем на рандомную строку
        wordsCursor.moveToFirst();
        wordsCursor.moveToPosition(r.nextInt(linesCount));

        //устаналиваем значение на родном языке и счетчик
        if (isReversed)  binding.fieldTop.setText(wordsCursor.getString(1));
        else  binding.fieldTop.setText(wordsCursor.getString(2));
        currentCount = 1;
        binding.counter.setText(currentCount + " / " + linesCount);
        binding.radioGroup.clearCheck();
        wrongWords.clear();
        shownList.add(wordsCursor.getInt(0));
        binding.rBtn1.setText("");
        binding.rBtn2.setText("");
        binding.rBtn3.setText("");
        binding.rBtn4.setText("");

        //выводим варианты ответа
        showOptions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение и курсор
        db.close();
        wordsCursor.close();
    }
}