package com.example.sqliteapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import com.example.sqliteapp.databinding.FragmentWordSearchBinding;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class WordSearchFragment extends Fragment implements View.OnTouchListener {

    private static final String TAG = "myLogs";
    private FragmentWordSearchBinding binding;
    final int CELLS_AMOUNT = 10; // количество ячеек по горизонтали и по вертикали
    final int LIMIT = 3; // минимальная длина слова
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    boolean isTheSame;
    char letter;
    int linesCount = 0, ver = 0, hor = 0, horCount = 0, verCount = 0, direction = 1;
    String currentWord, substr1, substr2;
    Random rand = new Random();
    Cell appropriateCell;
    Cell[] cellsArray = new Cell[CELLS_AMOUNT * CELLS_AMOUNT]; //массив для хранения ячеек
    ArrayList<Integer> usedList = new ArrayList<>(); //коллекция для хранения уже
    // использованных слов по айди в бд
    ArrayList<ArrayList<Cell>> insertedWordsList = new ArrayList<>();//коллекция
    // вставленных слов (их объектов Cells)
    float tableX, tableY, cellX, cellY;
    Float[] leftBordersArray = new Float[CELLS_AMOUNT];
    Float[] rightBordersArray = new Float[CELLS_AMOUNT];
    Float[] topBordersArray = new Float[CELLS_AMOUNT];
    Float[] bottomBordersArray = new Float[CELLS_AMOUNT];
    ArrayList<Cell> selectedInterval = new ArrayList<>(); //коллекция для записи текущего
    // выделенного слова
    ArrayList<Cell> earlierSelectedCells = new ArrayList<>(); //список для хранения ячеек,
    //выделенных во время предыдущих движений

    public WordSearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWordSearchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.create_db();
    }

    @Override
    public void onResume() {
        super.onResume();
        /*открываем БД*/
        db = databaseHelper.open();
        wordsCursor = db.rawQuery(
                "select * from " + DatabaseHelper.TABLE + " WHERE study = 1", null);
        wordsCursor.moveToFirst();
        linesCount = wordsCursor.getCount();
        /* проверка, что в бд не менее 20 слов*/
        if (wordsCursor.getCount() < 20) {
            binding.wsImpossible.setVisibility(View.VISIBLE);
            binding.wsText.setVisibility(View.GONE);
            binding.tableLayout.setVisibility(View.GONE);
        } else {
            fillArray(); //заполняем массив ячеек объектами

            for (Cell item: cellsArray) {

                item.getCellId().setOnTouchListener(this);
            }

            /* в цикле по очереди вставляем слова двумя разными способами: один добавляет слова так,
            чтобы они не пересекались с другими, второй сработает только если есть совпадающие
            буквы с другим словом */
            for (int j = 0; j < CELLS_AMOUNT * CELLS_AMOUNT; j++) {
                /* ДЛЯ ДОБАВЛЕНИЯ СЛОВ БЕЗ ПЕРЕСЕЧЕНИЙ*/
                clearVariables();
                getRandomDirection();
                getRandomWord();
                getRandomPosition();
                if (checkWordWithoutAppropriate()) { //если нужные ячейки свободны, то вставляем слово
                    writeWordWithoutAppropriate();
                }

                /*ДЛЯ ДОБАВЛЕНИЯ СЛОВ С ПЕРЕСЕЧЕНИЯМИ*/
                clearVariables();
                appropriateCell = null;
                getRandomWord(); // получаем рандомное слово
                /* подбираем ячейку, в которой вставлена буква, совпадающая с буквой в новом слове
                 объект appropriateCell */
                appropriateCell = getAppropriateCell();
                if (appropriateCell != null) { //если были найдены совпадающие буквы
                    placeWordWithAppropriate();
                    }
                }

            /* в конце выводим количество слов по гор. и вертикали в textView */
            binding.wsText.setText(getString(R.string.findWords, horCount, verCount));

            /* Заполняем оставшиеся ячейки случайными буквами, чтобы выглядело естественно, чередуем
            гласные и согласные, также убираем редко встречающиеся буквы */
            String consonants = "BCDFGHJKLMNPRST";
            String vowels = "AEIOU";
            char c;
            int counter = 1; //счетчик для чередования: в нечетные разы - гласные, в четные - согласные
            for (Cell item: cellsArray) {
                if (item.getLetter() == '0') {
                    if (counter % 2 != 0) {
                        c = vowels.charAt(rand.nextInt(vowels.length()));
                    } else {
                        c = consonants.charAt(rand.nextInt(consonants.length()));
                    }
                    item.setLetter(c);
                    item.getCellId().setText(Character.toString(c));
                    counter++;
                }
            }

            /* для обработки движений строим 4 массива, хранящих абсолютные границы столбцов и строк
            в пикселях. Делаем это после проверки, что tableLayout отрисован */
            ViewTreeObserver viewTreeObserver = binding.tableLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        /* получаем границы tableLayout */
                        tableX = binding.tableLayout.getX(); //в px
                        tableY = binding.tableLayout.getY(); //в px

                        /* получаем ширину столбцов и высоту строк */
                        float cellsHeight = cellsArray[0].getCellId().getHeight(); //в px
                        float cellsWidth = cellsArray[0].getCellId().getWidth(); //в px

                        /* получаем левые границы стобцов */
                        for (int m = 0; m < CELLS_AMOUNT; m++) {
                            leftBordersArray[m] = tableX + cellsWidth * m; //к левой границе таблицы
                            //прибавляем количество ячеек слева от текущей * на их ширину
                        }
                        /* получаем правые границы стобцов */
                        for (int m = 0; m < CELLS_AMOUNT; m++) {
                            rightBordersArray[m] = tableX + cellsWidth * (m + 1); //к левой границе таблицы
                            //прибавляем количество ячеек слева от текущей * на их ширину + еще одна ширина ячейки
                        }

                        /*аналогично с верхними и нижними границами */
                        for (int m = 0; m < CELLS_AMOUNT; m++) {
                            topBordersArray[m] = tableY + cellsHeight * m;
                        }

                        for (int m = 0; m < CELLS_AMOUNT; m++) {
                            bottomBordersArray[m] = tableY + cellsHeight * (m + 1);
                        }
                        Log.d(TAG, "leftBordersArray " + Arrays.deepToString(leftBordersArray));
                        Log.d(TAG, "topBordersArray " + Arrays.deepToString(topBordersArray));
                        Log.d(TAG, "bottomBordersArray " + Arrays.deepToString(bottomBordersArray));
                        Log.d(TAG, "rightBordersArray " + Arrays.deepToString(rightBordersArray));

                        binding.tableLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        }
    }


    public Cell getAppropriateCell() {
        /* проверяем, есть ли совпадающие буквы в таблице */
        for (Cell item: cellsArray) { //проверяем совпадение букв в уже заполненных ячейках с новым словом
            if (item.getLetter() != '0') { //проверка что ячейка не пустая
                for (int i = 0; i < currentWord.length(); i++) {
                    if (item.getLetter() == currentWord.charAt(i)) {
                        return item; //если подходящая ячейка найдена, выходим из метода
                    }
                }
            }
        }
        return null;
    }

    public void placeWordWithAppropriate() {
        /* разрезаем слово на 2 подстроки, до и после совпадающей буквы, не включая эту букву;
        если совпадает первая или последняя буква, то первая или вторая подстрока может оказаться
        пустой, в этом случае мы явно прописываем подстроки как пустые */
        int x = currentWord.indexOf(appropriateCell.getLetter());
        if (x == 0) {
            substr1 = "";
            substr2 = currentWord.substring(1);

        } else if (x == currentWord.length() - 1) {
            substr1 = currentWord.substring(0, currentWord.length() - 1);
            substr2 = ("");
        } else {
            substr1 = currentWord.substring(0, x);
            substr2 = currentWord.substring(x + 1);
        }

        Log.d(TAG, "первая подстрока = " + substr1);
        Log.d(TAG, "вторая подстрока = " + substr2);

        /* проверяем, не попадет ли последняя буква слова за границы поля
        /* назначаем переменные для проверки */
        boolean isTopDirectionAvailable = false,
                isLeftDirectionAvailable = false,
                isBottomDirectionAvailable = false,
                isRightDirectionAvailable = false;

        if (!substr1.equals("")) {
            /* проверяем верхнее направление */
            isTopDirectionAvailable = appropriateCell.getVer() - substr1.length() > -1;
            Log.d(TAG, "isTopDirectionAvailable = " + isTopDirectionAvailable);
            /* левое направление */
            isLeftDirectionAvailable = appropriateCell.getHor() - substr1.length() > -1;
            Log.d(TAG, "isLeftDirectionAvailable = " + isLeftDirectionAvailable);
        }
        if (!substr2.equals("")) {//substr2
            /* проверяем нижнее направление */
            isBottomDirectionAvailable = appropriateCell.getVer() + substr2.length() < CELLS_AMOUNT;
            Log.d(TAG, "isBottomDirectionAvailable = " + isBottomDirectionAvailable);

            /* правое направление */
            isRightDirectionAvailable = appropriateCell.getHor() + substr2.length() < CELLS_AMOUNT;
            Log.d(TAG, "isRightDirectionAvailable = " + isRightDirectionAvailable);
        }

        /*вычисляем, можно ли записать слово влево, вправо, вверх или вниз - все ли ячейки,
        в которые мы собираемся вставлять буквы, пустые */
        /* производим вычисление только при условии, что последняя буква слова не будет находиться
        за границами поля, иначе оставляем false */
        boolean areLeftCellsAvailable = false,
                areTopCellsAvailable = false,
                areRightCellsAvailable = false,
                areBottomCellsAvailable = false;

        if (isRightDirectionAvailable) {
            areRightCellsAvailable = checkRight();
        }
        if (isBottomDirectionAvailable) {
            areBottomCellsAvailable = checkBottom();
        }
        if (isLeftDirectionAvailable) {
            areLeftCellsAvailable = checkLeft();
        }
        if (isTopDirectionAvailable) {
            areTopCellsAvailable = checkTop();
        }

        /* запускаем функции, которые вписывают буквы в ячейки */
        /* УСЛОВИЕ 1. Если подстрока substr1 пустая, то вписываем substr2 вправо или вниз */
        if (substr1.equals("") && !substr2.equals("")) {
            if (areRightCellsAvailable) {//проверяем, можно ли вписать вправо
                /*записываем в horCount и в список использованных */
                insertedWordsList.add(writeRight());
                horCount++;
                Log.d(TAG, "horCount++ = " + horCount);
                usedList.add(wordsCursor.getInt(0));

            } else if (areBottomCellsAvailable) {
                /* иначе, если нельзя вписать вправо, проверяем возможность вписать вниз */
                insertedWordsList.add(writeBottom());
                verCount++; //если прошло успешно, записываем в verCount и в список использованных
                Log.d(TAG, "verCount++ = " + verCount);
                usedList.add(wordsCursor.getInt(0));
            }

            /* УСЛОВИЕ 2. Если подстрока substr2 пустая */
        } else if (!substr1.equals("") && substr2.equals("")) { //если пустая substr2
            if (areLeftCellsAvailable) {//проверяем, можно ли вписать влево
                insertedWordsList.add(writeLeft());
                horCount++;
                Log.d(TAG, "horCount++ = " + horCount);
                usedList.add(wordsCursor.getInt(0));
            } else if (areTopCellsAvailable) { //иначе, если нельзя вписать влево, вписываем вверх
                insertedWordsList.add(writeTop()); //одновременно вносим результат в список
                // insertedWordsList
                verCount++; //если прошло успешно, записываем в verCount и в список использованных
                Log.d(TAG, "verCount++ = " + verCount);
                usedList.add(wordsCursor.getInt(0));
            }

            /*УСЛОВИЕ 3. Если обе подстроки не пустые*/
        } else {
            if (areLeftCellsAvailable && areRightCellsAvailable) { //если можно вписать слева и справа
                ArrayList<Cell> a = (writeLeft());
                a.remove(a.size()-1);
                a.addAll(writeRight());
                insertedWordsList.add(a);
                horCount++;
                Log.d(TAG, "horCount++ = " + horCount);
                usedList.add(wordsCursor.getInt(0));

           } else if (areTopCellsAvailable && areBottomCellsAvailable) { //если возможность, то вписываем вверх и вниз
                ArrayList<Cell> a = (writeTop());
                a.remove(a.size()-1);
                a.addAll(writeBottom());
                insertedWordsList.add(a);
                verCount++;
                Log.d(TAG, "verCount++ = " + verCount);
                usedList.add(wordsCursor.getInt(0));
           }
        }
    }

    /*проверка, свободны ли ячейки в верхнем направлении */
    public boolean checkTop() {
        for (int i = appropriateCell.getVer() - substr1.length(), k = 0; k < substr1.length(); i++, k++) {
            letter = substr1.charAt(k); //получаем букву
            for (Cell item: cellsArray) {
                if (item.getVer() == i && item.getHor() == appropriateCell.getHor()) {
                    if (item.getLetter() != '0' && item.getLetter() != letter) {
                        //если вставлена другая буква, то выходим из метода
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /*вставляем буквы в верхнем направлении */
    public ArrayList<Cell> writeTop() {
        ArrayList<Cell> insertedLettersList = new ArrayList<>();
        for (int i = appropriateCell.getVer() - substr1.length(), k = 0; k < substr1.length(); i++, k++) {
            letter = substr1.charAt(k); //получаем букву
            for (Cell item: cellsArray) {
                if (item.getVer() == i && item.getHor() == appropriateCell.getHor()) {
                    item.getCellId().setText(Character.toString(letter)); // в ячейку
                    item.setLetter(letter);// и в объект
                    insertedLettersList.add(item);
                }
            }
        }
        insertedLettersList.add(appropriateCell);
        return(insertedLettersList);
    }

    /* проверка, свободны ли ячейки в левом направлении */
    public boolean checkLeft() {
        for (int i = appropriateCell.getHor() - substr1.length(), k = 0; k < substr1.length(); i++, k++) {
            letter = substr1.charAt(k); //получаем букву
            for (Cell item: cellsArray) {
                if (item.getVer() == appropriateCell.getVer() && item.getHor() == i) {
                    if (item.getLetter() == '0') {// проверка на незанятость ячейки, если
                        // ячейка пустая, идем проверять следующую
                        break;
                    } else if (item.getLetter() == letter) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        // идем проверять следующую
                        break;
                    } else if (item.getLetter() != '0' && item.getLetter() != letter) {
                        //если вставлена другая буква, то метод возвращает false
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /*вставляем буквы в левом направлении */
    public ArrayList<Cell> writeLeft() {
        ArrayList<Cell> insertedLettersList = new ArrayList<>();
        for (int i = appropriateCell.getHor() - substr1.length(), k = 0; k < substr1.length(); i++, k++) {
            letter = substr1.charAt(k); //получаем букву
            for (Cell item: cellsArray) {
                if (item.getVer() == appropriateCell.getVer() && item.getHor() == i) {
                    item.getCellId().setText(Character.toString(letter)); // в ячейку
                    item.setLetter(letter);// и в объект
                    insertedLettersList.add(item);
                }
            }
        }
        insertedLettersList.add(appropriateCell);
        return(insertedLettersList);
    }

    /*проверка, свободны ли ячейки в правом направлении */
    public boolean checkRight() {
        for (int i = appropriateCell.getHor() + 1, k = 0; k < substr2.length(); i++, k++) {
            letter = substr2.charAt(k); //получаем букву
            for (Cell item: cellsArray) { //находим объект cell с координатами appropriateCell.getHor
                // и i по вертикали
                if (item.getVer() == appropriateCell.getVer() && item.getHor() == i) {
                    if (item.getLetter() == '0') {// проверка на незанятость ячейки
                        //если ячейка пустая, выходим из цикла
                        break;
                    } else if (item.getLetter() == letter) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        // если совпадают, выходим из цикла
                        break;
                    } else if (item.getLetter() != '0' && item.getLetter() != letter) {
                        //если вставлена другая буква, то выходим из метода и возращаем тру
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /*вставляем буквы в правом направлении */
    public ArrayList<Cell> writeRight() {
        ArrayList<Cell> insertedLettersList = new ArrayList<>();
        insertedLettersList.add(appropriateCell);
        for (int i = appropriateCell.getHor() + 1, k = 0; k < substr2.length(); i++, k++) {
            letter = substr2.charAt(k); //получаем букву
            for (Cell item: cellsArray) { //находим объект cell
                if (item.getVer() == appropriateCell.getVer() && item.getHor() == i) {
                    item.getCellId().setText(Character.toString(letter)); // в ячейку
                    item.setLetter(letter);// и в объект
                    insertedLettersList.add(item);
                }
            }
        }
        return(insertedLettersList);
    }

    /*проверка, свободны ли ячейки в нижнем направлении */
    public boolean checkBottom() {
        for (int i = appropriateCell.getVer() + 1, k = 0; k < substr2.length(); i++, k++) {
            letter = substr2.charAt(k); //получаем букву
            for (Cell item: cellsArray) { //находим объект cell
                if (item.getVer() == i && item.getHor() == appropriateCell.getHor()) {
                    if (item.getLetter() == '0') {// проверка на незанятость ячейки
                        //если ячейка пустая, выходим из внутреннего цикла и проверяем дальше
                        break;
                    } else if (item.getLetter() == (letter)) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        // то выходим из цикла и проверяем дальше
                        break;
                    } else if (item.getLetter() != '0' && item.getLetter() != letter) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /*вставляем буквы в нижнем направлении */
    public ArrayList<Cell> writeBottom() {
        ArrayList<Cell> insertedLettersList = new ArrayList<>();
        insertedLettersList.add(appropriateCell);
        for (int i = appropriateCell.getVer() + 1, k = 0; k < substr2.length(); i++, k++) {
            letter = substr2.charAt(k); //получаем букву
            for (Cell item: cellsArray) { //находим объект cell
                if (item.getVer() == i && item.getHor() == appropriateCell.getHor()) {
                    item.getCellId().setText(Character.toString(letter)); // в ячейку
                    item.setLetter(letter);// и в объект
                    insertedLettersList.add(item);
                }
            }
        }
        return(insertedLettersList);
    }

    /*проверка для вставки слов без поиска пересечений */
    public boolean checkWordWithoutAppropriate() {
        if (direction == 1) { //для гориз.ориентации
            for (int i = hor, k = 0; k < currentWord.length(); i++, k++) {
                letter = currentWord.charAt(k); //получаем букву
                for (Cell item: cellsArray) { //находим объект cell с координатами i и ver
                    if (item.getHor() == i && item.getVer() == ver) {
                        if (item.getLetter() == '0') {// проверка на незанятость ячейки
                            break; //выходим из цикла и переходим к следующей ячейке
                        } else if (item.getLetter() == letter) {
                            //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                            // то переходим к следующей ячейке на пути - выходим из цикла
                            break;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }

        if (direction == 2) {//для верт.ориентации
            for (int i = ver, k = 0; k < currentWord.length(); i++, k++) {
                letter = currentWord.charAt(k); //получаем букву
                for (Cell item: cellsArray) { //находим объект cell с координатами hor и i
                    if (item.getVer() == i && item.getHor() == hor) {
                        if (item.getLetter() == '0') {// проверка на незанятость ячейки
                            break; //выходим из цикла и переходим к следующей ячейке
                        } else if (item.getLetter() == letter) {
                            //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                            // то переходим к следующей ячейке на пути - выходим из цикла
                            break;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /*вставляем слово без поиска пересечений */
    public void writeWordWithoutAppropriate() {
        ArrayList<Cell> insertedLettersList = new ArrayList<>();

        if (direction == 1) { //для гориз.ориентации
            for (int i = hor, k = 0; k < currentWord.length(); i++, k++) {
                letter = currentWord.charAt(k); //получаем букву
                for (Cell item: cellsArray) { //находим объект cell с координатами i и ver
                    if (item.getVer() == ver && item.getHor() == i) {
                        item.getCellId().setText(Character.toString(letter));
                        item.setLetter(letter);
                        insertedLettersList.add(item);
                    }
                }
            }
        }
        if (direction == 2) {//для верт.ориентации
            for (int i = ver, k = 0; k < currentWord.length(); i++, k++) {
                letter = currentWord.charAt(k); //получаем букву
                for (Cell item: cellsArray) { //находим объект cell с координатами hor и i
                    if (item.getVer() == i && item.getHor() == hor) {
                        item.getCellId().setText(Character.toString(letter));
                        item.setLetter(letter);
                        insertedLettersList.add(item);
                    }
                }
            }
        }
        /* в конце записываем в коллекцию использованных */
        usedList.add(wordsCursor.getInt(0));
        insertedWordsList.add(insertedLettersList);
        /* и в количество горизонтально или вертикально расположенных слов */
        if (direction == 1) {
            horCount++;
            Log.d(TAG, "horCount = " + horCount);
        } else if (direction == 2) {
            verCount++;
            Log.d(TAG, "verCount = " + verCount);
        }
    }

    /*получаем рандомное слово из курсора */
    public void getRandomWord() {
        do {
            wordsCursor.moveToPosition(rand.nextInt(linesCount));
            currentWord = wordsCursor.getString(1);
        } while (checkUsed() ||
                currentWord.length() > CELLS_AMOUNT ||
                currentWord.length() < LIMIT ||
                StringUtils.containsAny(currentWord, "/", ":", ";", "$", "%", "@",
                        "^", "{", "}", "(", ")", "*", "&", "?", ".", ",", "!", "<", ">", "|", "+",
                        "=", "-", "~", "№", "#", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                        "\\", "\"", "'")); //если строка содержит символы, не используем ее
        currentWord = currentWord.trim();
        if (currentWord.contains(" ")) { //если строка состоит из нескольких слов, разделенных
            // пробелами, нарезаем на подстроки
            String[] currentWordSplit = StringUtils.split(currentWord, ' ');
            for (String s: currentWordSplit) {
                if (s.length() < CELLS_AMOUNT && s.length() > LIMIT) {
                    currentWord = s; //находим первое слово подходящей длины
                    break;                    //и выходим из цикла
                }
            }
        }
        currentWord = currentWord.toUpperCase(); //во избежание путаницы используем всегда заглавные буквы
    }

    /*получаем случайную ячейку для вставки слова без поиска пересечений */
    public void getRandomPosition() {
        // генерируем координаты для первой буквы
        if (direction == 1) { //если ориент. гориз.
            hor = rand.nextInt(CELLS_AMOUNT - currentWord.length()); // генерируем
            // значение в таком диапазоне, чтобы слово не вышло за пределы поля
            ver = rand.nextInt(CELLS_AMOUNT); // по вертикали любое значение
        }

        if (direction == 2) { // если слово располагаем по вертикали, тогда наоборот
            ver = rand.nextInt(CELLS_AMOUNT - currentWord.length());
            hor = rand.nextInt(CELLS_AMOUNT); // по горизонтали любое значение
        }
    }

    /*случайно генерируем направление - вверх или вниз */
    public void getRandomDirection() {
        direction = rand.nextInt(2) + 1;
    }

    /* проверяем, было ли уже использовано это слово в этом туре игры */
    public boolean checkUsed() {
        boolean isUsed = false;
        for (int i = 0, usedSize = usedList.size(); i < usedSize; i++) {
            if (usedList.get(i) == wordsCursor.getInt(0)) {
                isUsed = true;
                break;
            }
        }
        return isUsed;
    }

    public void clearVariables() {
        currentWord = "";
        letter = '0';
        substr1 = "";
        substr2 = "";
        hor = 0;
        ver = 0;
        isTheSame = false;
    }

    public void fillArray() {
        cellsArray[0] = new Cell(0, 0, binding.v0h0, '0');
        cellsArray[1] = new Cell(0, 1, binding.v0h1, '0');
        cellsArray[2] = new Cell(0, 2, binding.v0h2, '0');
        cellsArray[3] = new Cell(0, 3, binding.v0h3, '0');
        cellsArray[4] = new Cell(0, 4, binding.v0h4, '0');
        cellsArray[5] = new Cell(0, 5, binding.v0h5, '0');
        cellsArray[6] = new Cell(0, 6, binding.v0h6, '0');
        cellsArray[7] = new Cell(0, 7, binding.v0h7, '0');
        cellsArray[8] = new Cell(0, 8, binding.v0h8, '0');
        cellsArray[9] = new Cell(0, 9, binding.v0h9, '0');
        cellsArray[10] = new Cell(1, 0, binding.v1h0, '0');
        cellsArray[11] = new Cell(1, 1, binding.v1h1, '0');
        cellsArray[12] = new Cell(1, 2, binding.v1h2, '0');
        cellsArray[13] = new Cell(1, 3, binding.v1h3, '0');
        cellsArray[14] = new Cell(1, 4, binding.v1h4, '0');
        cellsArray[15] = new Cell(1, 5, binding.v1h5, '0');
        cellsArray[16] = new Cell(1, 6, binding.v1h6, '0');
        cellsArray[17] = new Cell(1, 7, binding.v1h7, '0');
        cellsArray[18] = new Cell(1, 8, binding.v1h8, '0');
        cellsArray[19] = new Cell(1, 9, binding.v1h9, '0');
        cellsArray[20] = new Cell(2, 0, binding.v2h0, '0');
        cellsArray[21] = new Cell(2, 1, binding.v2h1, '0');
        cellsArray[22] = new Cell(2, 2, binding.v2h2, '0');
        cellsArray[23] = new Cell(2, 3, binding.v2h3, '0');
        cellsArray[24] = new Cell(2, 4, binding.v2h4, '0');
        cellsArray[25] = new Cell(2, 5, binding.v2h5, '0');
        cellsArray[26] = new Cell(2, 6, binding.v2h6, '0');
        cellsArray[27] = new Cell(2, 7, binding.v2h7, '0');
        cellsArray[28] = new Cell(2, 8, binding.v2h8, '0');
        cellsArray[29] = new Cell(2, 9, binding.v2h9, '0');
        cellsArray[30] = new Cell(3, 0, binding.v3h0, '0');
        cellsArray[31] = new Cell(3, 1, binding.v3h1, '0');
        cellsArray[32] = new Cell(3, 2, binding.v3h2, '0');
        cellsArray[33] = new Cell(3, 3, binding.v3h3, '0');
        cellsArray[34] = new Cell(3, 4, binding.v3h4, '0');
        cellsArray[35] = new Cell(3, 5, binding.v3h5, '0');
        cellsArray[36] = new Cell(3, 6, binding.v3h6, '0');
        cellsArray[37] = new Cell(3, 7, binding.v3h7, '0');
        cellsArray[38] = new Cell(3, 8, binding.v3h8, '0');
        cellsArray[39] = new Cell(3, 9, binding.v3h9, '0');
        cellsArray[40] = new Cell(4, 0, binding.v4h0, '0');
        cellsArray[41] = new Cell(4, 1, binding.v4h1, '0');
        cellsArray[42] = new Cell(4, 2, binding.v4h2, '0');
        cellsArray[43] = new Cell(4, 3, binding.v4h3, '0');
        cellsArray[44] = new Cell(4, 4, binding.v4h4, '0');
        cellsArray[45] = new Cell(4, 5, binding.v4h5, '0');
        cellsArray[46] = new Cell(4, 6, binding.v4h6, '0');
        cellsArray[47] = new Cell(4, 7, binding.v4h7, '0');
        cellsArray[48] = new Cell(4, 8, binding.v4h8, '0');
        cellsArray[49] = new Cell(4, 9, binding.v4h9, '0');
        cellsArray[50] = new Cell(5, 0, binding.v5h0, '0');
        cellsArray[51] = new Cell(5, 1, binding.v5h1, '0');
        cellsArray[52] = new Cell(5, 2, binding.v5h2, '0');
        cellsArray[53] = new Cell(5, 3, binding.v5h3, '0');
        cellsArray[54] = new Cell(5, 4, binding.v5h4, '0');
        cellsArray[55] = new Cell(5, 5, binding.v5h5, '0');
        cellsArray[56] = new Cell(5, 6, binding.v5h6, '0');
        cellsArray[57] = new Cell(5, 7, binding.v5h7, '0');
        cellsArray[58] = new Cell(5, 8, binding.v5h8, '0');
        cellsArray[59] = new Cell(5, 9, binding.v5h9, '0');
        cellsArray[60] = new Cell(6, 0, binding.v6h0, '0');
        cellsArray[61] = new Cell(6, 1, binding.v6h1, '0');
        cellsArray[62] = new Cell(6, 2, binding.v6h2, '0');
        cellsArray[63] = new Cell(6, 3, binding.v6h3, '0');
        cellsArray[64] = new Cell(6, 4, binding.v6h4, '0');
        cellsArray[65] = new Cell(6, 5, binding.v6h5, '0');
        cellsArray[66] = new Cell(6, 6, binding.v6h6, '0');
        cellsArray[67] = new Cell(6, 7, binding.v6h7, '0');
        cellsArray[68] = new Cell(6, 8, binding.v6h8, '0');
        cellsArray[69] = new Cell(6, 9, binding.v6h9, '0');
        cellsArray[70] = new Cell(7, 0, binding.v7h0, '0');
        cellsArray[71] = new Cell(7, 1, binding.v7h1, '0');
        cellsArray[72] = new Cell(7, 2, binding.v7h2, '0');
        cellsArray[73] = new Cell(7, 3, binding.v7h3, '0');
        cellsArray[74] = new Cell(7, 4, binding.v7h4, '0');
        cellsArray[75] = new Cell(7, 5, binding.v7h5, '0');
        cellsArray[76] = new Cell(7, 6, binding.v7h6, '0');
        cellsArray[77] = new Cell(7, 7, binding.v7h7, '0');
        cellsArray[78] = new Cell(7, 8, binding.v7h8, '0');
        cellsArray[79] = new Cell(7, 9, binding.v7h9, '0');
        cellsArray[80] = new Cell(8, 0, binding.v8h0, '0');
        cellsArray[81] = new Cell(8, 1, binding.v8h1, '0');
        cellsArray[82] = new Cell(8, 2, binding.v8h2, '0');
        cellsArray[83] = new Cell(8, 3, binding.v8h3, '0');
        cellsArray[84] = new Cell(8, 4, binding.v8h4, '0');
        cellsArray[85] = new Cell(8, 5, binding.v8h5, '0');
        cellsArray[86] = new Cell(8, 6, binding.v8h6, '0');
        cellsArray[87] = new Cell(8, 7, binding.v8h7, '0');
        cellsArray[88] = new Cell(8, 8, binding.v8h8, '0');
        cellsArray[89] = new Cell(8, 9, binding.v8h9, '0');
        cellsArray[90] = new Cell(9, 0, binding.v9h0, '0');
        cellsArray[91] = new Cell(9, 1, binding.v9h1, '0');
        cellsArray[92] = new Cell(9, 2, binding.v9h2, '0');
        cellsArray[93] = new Cell(9, 3, binding.v9h3, '0');
        cellsArray[94] = new Cell(9, 4, binding.v9h4, '0');
        cellsArray[95] = new Cell(9, 5, binding.v9h5, '0');
        cellsArray[96] = new Cell(9, 6, binding.v9h6, '0');
        cellsArray[97] = new Cell(9, 7, binding.v9h7, '0');
        cellsArray[98] = new Cell(9, 8, binding.v9h8, '0');
        cellsArray[99] = new Cell(9, 9, binding.v9h9, '0');
    }

    /* окрашиваем ячейки, когда пользователь заводит палец в ячейку */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        view.performClick();
        Cell firstCell = new Cell();//объект указывает на первую ячейку, с
        // которой пользователь начал введение слова
        //когда пользователь проводит по слову, нужно изменить фон ячеек. Чтобы получить view ячеек,
        // возьмем координаты их границ
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: // нажатие
                selectedInterval.clear(); //очищаем список selectedInterval при начале каждого нового движения
                view.setBackgroundColor(getResources().getColor(R.color.teal_200));//окрашиваем
                for (Cell item: cellsArray) { //вычисляем объект-ячейку по view
                    if (view == item.getCellId()) {
                        firstCell = item;
                        selectedInterval.add(firstCell);
                        Log.d(TAG, "ACTION_DOWN" + view);
                        break;
                    }
                }

                for (int i = 0; i < leftBordersArray.length; i++) { //находим кординаты на экране
                    // в пикселях первой ячейки
                    if (firstCell.getHor() == i) {
                        cellX = leftBordersArray[i];
                    }
                }

                for (int i = 0; i < topBordersArray.length; i++) { //находим кординаты на экране
                    // в пикселях первой ячейки
                    if (firstCell.getVer() == i) {
                        cellY = topBordersArray[i];
                    }
                }
                Log.d(TAG, "cellX " + cellX);
                Log.d(TAG, "cellY  " + cellY);
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                Log.d(TAG, "ACTION_MOVE " + x + "," + y);
                Log.d(TAG, "ACTION_MOVE view " + view);
                break;

            case MotionEvent.ACTION_MOVE: // движение
                /* при каждом событии перемещения рассчитываем ячейку, в которой находится палец,
                по координатам */
                float p = motionEvent.getX() + cellX; //получаем координаты точки по оси X
                // на экране, прибавляя координаты первой ячейки
                float q = motionEvent.getY() + cellY;
                Log.d(TAG, "ACTION_MOVE " + p + "," + q);

                int curCellHor = 0,
                        curCellVer = 0;

                for (int a = 0; a < CELLS_AMOUNT; a++) {
                    if (leftBordersArray[a] <= p && rightBordersArray[a] >= p) {
                        curCellHor = a;
                        break;
                    }
                }

                for (int b = 0; b < CELLS_AMOUNT; b++) {
                    if (topBordersArray[b] <= q && bottomBordersArray[b] >= q) {
                        curCellVer = b;
                        break;
                    }
                }
                Log.d(TAG, "ACTION_MOVE Cell " + curCellVer + "," + curCellHor);

                /*окрашиваем ячейку, в которую завели палец */
                for (Cell cell: cellsArray) {
                    if (cell.getVer() == curCellVer && cell.getHor() == curCellHor) { //находим
                        // ячейку перебором массива
                        if (//сравниваем текущую ячейку с последней в списке
                            !cell.equals(selectedInterval.get(selectedInterval.size() - 1))) {
                            //Если текущая ячейка уже была записана в selectedInterval в процессе
                            // текущего движения, то есть является в нем последней, то пропускаем
                            // ее, иначе записываем в список и окрашиваем:
                            selectedInterval.add(cell);
                            Log.d(TAG, "selectedInterval.size() " + selectedInterval.size());
                            cell.getCellId()
                                    .setBackgroundColor(getResources().getColor(R.color.teal_200));
                            Log.d(TAG, "ACTION_MOVE Cell " + cell.getCellId());
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP: // отпускание
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "selectedInterval.size() " + selectedInterval.size());
                Log.d(TAG, "selectedInterval.toString " + selectedInterval.toString());
                Log.d(TAG, "earlierSelectedCells.size() " + earlierSelectedCells.size());
                /* проверяем, совпадает ли список selectedInterval с каким-нибудь списком
                в insertedWordsList */
                if (!insertedWordsList.contains(selectedInterval)) {
                    /* выводим сообщение об ошибке */
                    Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_SHORT).show();
                    /* заменяем зеленый фон у выделенных ячеек на первоначальный */
                    for (Cell cell: selectedInterval) {
                        cell.getCellId().setBackgroundResource(R.drawable.cell_border);
                        /*кроме тех, которые были выделены раньше в составе других слов, они
                        хранятся в earlierSelectedCells */
                        if (earlierSelectedCells.size() != 0) {
                            for (Cell item: earlierSelectedCells) {
                                item.getCellId().setBackgroundColor(getResources()
                                        .getColor(R.color.teal_200));
                            }
                        }
                    }
                } else {
                    earlierSelectedCells.addAll(selectedInterval);
                }
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        db.close();
    }
}