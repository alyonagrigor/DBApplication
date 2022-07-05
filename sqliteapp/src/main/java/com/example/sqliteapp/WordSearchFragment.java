package com.example.sqliteapp;

/**
 * утечка из бд!
 * возможно перееисать на стрингбилдер
 * переписать основной метод
 * возможно добавить генерацию букв из др.алфавитов
 * возм. добавить вычеркивание
 * возм. запретить персечение в одной ориентации
 * возм. увеличить до 12 на 12
 * доб. вырезание слов из предложения и проверку на отсут.цифр
 * доб. проверку размера бд
 *
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sqliteapp.databinding.FragmentWordSearchBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class WordSearchFragment extends Fragment {

    private static final String TAG = "myLogs";
    private FragmentWordSearchBinding binding;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    int linesCount = 0, ver = 0, hor = 0, horCount = 0, verCount = 0, direction = 1;
    final int CELLS_AMOUNT = 10; // количество ячеек по горизонтали и по вертикали
    final int LIMIT = 3; // минимальная длина слова
    final int TOTAL_TRIES_AMOUNT = 200; //сколько раз нужно прогнать главный цикл после вставки
    // первого слова
    boolean isUsed, hasSucceed, isTheSame;
    String currentWord, letter, substr1, substr2;
    Random r = new Random();
    Cell[] array = new Cell[CELLS_AMOUNT * CELLS_AMOUNT]; //массив для хранения ячеек
    Cell appropriateCell;
    ArrayList<Integer> usedList = new ArrayList<Integer>(); //коллекция для хранения уже
    // использованных слов по айди в бд
    ArrayList<Cell> appropriateCellsList = new ArrayList<Cell>(); //коллекция appropriateCells,
    //использованных в данном цикле

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
        createArray();
    }

    @Override
    public void onResume() {
        super.onResume();
        db = databaseHelper.open();
        wordsCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE
                        // нужно ли исключать слова?
                        + " WHERE study = 1"
                , null);
        wordsCursor.moveToFirst();
        linesCount = wordsCursor.getCount();
        /* ...проверка, что в бд не менее TOTAL_TRIES_AMOUNT + 1 слов умножить на 2 */

        // получаем рандомное слово
        getRandomWord();
        //первое слово всегда в горизонтальной ориентации
        direction = 1;
        //генерируем рандомную позицию
        getRandomPosition();
        //вставляем первое слово
        placeFirstWord();

        //пытаемся вставить еще шесть слов по горизонтали
        for (int i = 0; i < 6; i++)  {
            clearVariables();
            getRandomWord();
            getRandomPosition();
            if (!checkWordWithoutAppropriate()) {
             writeWordWithoutAppropriate();
            }
        }

        /* БОЛЬШОЙ ЦИКЛ ДЛЯ ДОБАВЛЕНИЯ ДАЛЬНЕЙШИХ СЛОВ ПОСЛЕ ПЕРВОГО !!!!!!!!!!!!!!!!!!!!!!!!!!!
        цикл просто прогоняется TOTAL_TRIES_AMOUNT раз, в какие-то
         из этих TOTAL_TRIES_AMOUNT раз слово будет вставлено, а в какие-то - нет */
         for (int n = 0; n < TOTAL_TRIES_AMOUNT; n++) {
            //обнуляем переменные
            clearVariables();
            appropriateCell = null;
            Log.d(TAG, "запускаем цикл раз номер " + n);
            // получаем рандомное слово
            getRandomWord();
            Log.d(TAG, "получаем слово номкр " + n + " " + currentWord);
            //подбираем ячейку, в которой вставлена буква, совпадающая с буквой в новом слове -
            //объект appropriateCell
            findAppropriateCell();

            //получаем (или не получаем) объект appropriateCell
            if (appropriateCell == null) {
                Log.d(TAG, "получаем appropriateCell == null");
                //-------------ВЕРОЯТНОСТЬ ЭТОГО СЦЕНАРИЯ ПРАКТИЧЕСКИ РАВНА НУЛЮ
                getRandomPosition();
                if (!checkWordWithoutAppropriate()) {
                    writeWordWithoutAppropriate();
                }

            } else { //если есть совпадающие буквы (appropriateCell == !null)
                appropriateCellsList.add(appropriateCell);
                boolean bool = placeWordWithAppropriate();
                //если с первой попытки вставить не удалось, то пробуем подобрать другую
                // appropriateCell CELLS_AMOUNT * CELLS_AMOUNT раз
                if (!bool) {
                    appropriateCell = null;
                    for (int y = 0; y < CELLS_AMOUNT * CELLS_AMOUNT; y++) {
                        findAppropriateCell();
                        if (appropriateCell != null) {
                            //цикл для проверки, что это не та же ячейка
                            isTheSame = false;
                            for (int i = 0, listSize = appropriateCellsList.size(); i < listSize; i++) {
                                Cell item = appropriateCellsList.get(i);
                                if (item.getCellId() == appropriateCell.getCellId()) {
                                    isTheSame = true;
                                    break;
                                }
                            }
                            appropriateCellsList.add(appropriateCell);
                            break;
                        }
                    }
                }
                // если удалось подобрать appropriateCell, то пытаемся вставить еще 1 раз
                if (!hasSucceed && appropriateCell != null && !isTheSame) {
                    clearVariables();
                    placeWordWithAppropriate();
                }
                //конец логики, основной цикл запускается снова
            }
        }
        /* КОНЕЦ ЦИКЛА!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */


        //в конце выводим количество слов по гор. и вертикали в textView
        binding.wsText.setText(getString(R.string.findWords, horCount, verCount));

        //и заполняем оставшиеся ячейки случайными буквами
    /*    for (Cell item: array) {
            if (item.getLetter().equals("")) {
                //генерируем случайную букву латинского алфавита
                String str = String.valueOf((char)(r.nextInt(26) + 'A'));
                item.setLetter(str);
                item.getCellId().setText(str);
            }
        }*/

    } // конец ONRESUME//////////////////////////////////////////////////////////////////

    public void findAppropriateCell() {
        //проверяем, есть ли совпадающие буквы в таблице
        for (Cell item: array) { //проверяем совпадение букв в уже заполненных ячейках с новым словом
            if (!item.getLetter().equals("")) { //проверка что ячейка не пустая
                for (int i = 0; i < currentWord.length(); i++) {
                    if (item.getLetter().equals(Character.toString(currentWord.charAt(i)))) {
                        appropriateCell = new Cell(item.getHor(), item.getVer(),
                                item.getCellId(), item.getLetter());
                        break; //если подходящая ячейка найдена, выходим из внутреннего цикла
                    }
                }
                if (appropriateCell != null) {
                    break;
                } //если подходящая ячейка найдена, выходим из внешнего цикла
            }
        }
    }

    public boolean placeWordWithAppropriate() {
        //разрезаем слово на 2 подстроки, до и после совпадающей буквы, не включая эту букву;
        //если сопадает первая или последняя буква, то первая или вторая подстрока может оказаться
        // пустой, в этом случае мы явно прописываем подстроки как пустые
        int x = currentWord.indexOf(appropriateCell.getLetter());
        if (x == 0) {
            substr1 = "";
            substr2 = currentWord.substring(1);
            Log.d(TAG, "первая подстрока = " + substr1);
            Log.d(TAG, "вторая подстрока = " + substr2);
        } else if (x == currentWord.length() - 1) {
            substr1 = currentWord.substring(0, currentWord.length() - 1);
            substr2 = ("");
            Log.d(TAG, "первая подстрока = " + substr1);
            Log.d(TAG, "вторая подстрока = " + substr2);
        } else {
            substr1 = currentWord.substring(0, x);
            substr2 = currentWord.substring(x + 1);
            Log.d(TAG, "первая подстрока = " + substr1);
            Log.d(TAG, "вторая подстрока = " + substr2);
        }

        //проверяем, не попадет ли последняя буква слова за границы поля
        //назначаем переменные для провреки
        boolean isTopDirectionAvailable = false;
        Log.d(TAG, "isTopDirectionAvailable = " + isTopDirectionAvailable);
        boolean isLeftDirectionAvailable = false;
        Log.d(TAG, "isLeftDirectionAvailable = " + isLeftDirectionAvailable);
        isLeftDirectionAvailable = appropriateCell.getVer() - substr1.length() > 0;
        boolean isBottomDirectionAvailable = false;
        Log.d(TAG, "isBottomDirectionAvailable = " + isBottomDirectionAvailable);
        boolean isRightDirectionAvailable = false;
        Log.d(TAG, "isRightDirectionAvailable = " + isRightDirectionAvailable);

        if (!substr1.equals("")) {
            //проверяем верхнее направление
            isTopDirectionAvailable = appropriateCell.getHor() - substr1.length() > 0;
            Log.d(TAG, "isTopDirectionAvailable = " + isTopDirectionAvailable);
            //левое направление
            isLeftDirectionAvailable = appropriateCell.getVer() - substr1.length() > 0;
            Log.d(TAG, "isLeftDirectionAvailable = " + isLeftDirectionAvailable);
        }
        if (!substr2.equals("")) {//substr2
            //проверяем нижнее направление
            isBottomDirectionAvailable = appropriateCell.getHor() + substr2.length() < CELLS_AMOUNT + 1;
            Log.d(TAG, "isBottomDirectionAvailable = " + isBottomDirectionAvailable);

            //правое направление
            isRightDirectionAvailable = appropriateCell.getVer() + substr2.length() < CELLS_AMOUNT + 1;
            Log.d(TAG, "isRightDirectionAvailable = " + isRightDirectionAvailable);
        }

        //запускаем функции, которые вписывают буквы в ячейки
        //УСЛОВИЕ 1. Если подстрока substr1 пустая
        if (substr1.equals("") && !substr2.equals("")) {
            //  то вписываем substr2 вправо или вниз
            //если оба направления недоступны, возвращаем false
            if (!isRightDirectionAvailable && !isBottomDirectionAvailable) {
                hasSucceed = false; //конец логики метода
            } else {
                //вычисляем, можно ли записать вправо или вниз
                boolean checkRightResult = checkRight(),
                        checkBottomResult = checkBottom();
                if (checkBottomResult && checkRightResult) {//если нельзя ни вправо, ни вниз, то
                    hasSucceed = false; //конец логики метода
                }
                if (isRightDirectionAvailable && !checkRightResult) {//проверяем, можно ли вписать вправо
                    writeRight();
                    horCount++;
                    Log.d(TAG, "horCount++ = " + horCount);
                    usedList.add(wordsCursor.getInt(0));
                    //если прошло успешно, записываем в horCount и в список использованных
                    hasSucceed = true;//конец логики метода
                } else if ((checkRightResult || !isRightDirectionAvailable)
                        && isBottomDirectionAvailable && !checkBottomResult) {
                    //если нельзя вписать вправо, пытаемся вписать вниз
                    writeBottom();
                    verCount++; //если прошло успешно, записываем в verCount и в список использованных
                    Log.d(TAG, "verCount++ = " + verCount);
                    usedList.add(wordsCursor.getInt(0));
                    hasSucceed = true; //конец логики метода
                } else if ((checkRightResult || !isRightDirectionAvailable)
                        && (!isBottomDirectionAvailable || checkBottomResult)) {
                    //если нельзя вписать никуда, то
                    hasSucceed = false;
                }
            }


            //УСЛОВИЕ 2. Если подстрока substr2 пустая
        } else if (!substr1.equals("") && substr2.equals("")) { //если пустая substr2
            //  то вписываем substr1 влево
            //если оба направления недоступны, возвращаем false
            if (!isLeftDirectionAvailable && !isTopDirectionAvailable) {
                hasSucceed = false; //конец логики метода
            } else {
                //вычисляем, можно ли записать влево или вверх
                boolean checkLeftResult = checkLeft(),
                        checkTopResult = checkTop();
                if (checkTopResult && checkLeftResult) {//если в оба направления вписать нельзя, то
                    hasSucceed = false;
                } //конец логики метода
                if (isLeftDirectionAvailable && !checkLeftResult) {//если влево вписать можно, то
                    writeLeft();
                    horCount++;
                    Log.d(TAG, "horCount++ = " + horCount);
                    usedList.add(wordsCursor.getInt(0));
                    //если прошло успешно, записываем в horCount и в список использованных
                    hasSucceed = true;//конец логики метода
                    //если влево вписать нельзя, вписываем вврх
                } else if ((checkLeftResult || !isLeftDirectionAvailable)
                        && isTopDirectionAvailable && !checkTopResult) {
                    writeTop();
                    verCount++;
                    Log.d(TAG, "verCount++ = " + verCount);
                    usedList.add(wordsCursor.getInt(0));
                    hasSucceed = true;
                } else if ((checkLeftResult || !isLeftDirectionAvailable)
                        && (!isTopDirectionAvailable || checkTopResult)) {
                    //если нельзя вписать никуда, то
                    hasSucceed = false;
                }
            }

            //УСЛОВИЕ 3. Если обе подстроки не пустые
        } else if ((!substr1.equals("") && !substr2.equals(""))) {
            //проверяем, доступны ли направления
            //если оба направления недоступны, возвращаем false
            if ((!isLeftDirectionAvailable || !isRightDirectionAvailable)
                    && (!isTopDirectionAvailable || !isBottomDirectionAvailable)) {
                hasSucceed = false; //конец логики метода
            } else {
                boolean checkLeftResult = checkLeft(),
                        checkTopResult = checkTop(),
                        checkRightResult = checkRight(),
                        checkBottomResult = checkBottom();

                if ((checkTopResult || checkBottomResult)
                        && (checkLeftResult || checkRightResult)) {//если в оба направления вписать нельзя, то
                    hasSucceed = false;
                } //конец логики метода
                //вписываем горизонтально
                if (isLeftDirectionAvailable && isRightDirectionAvailable
                        && !checkLeftResult && !checkRightResult) { //если можно вписать слева и справа, то
                    writeRight();
                    writeLeft();
                    horCount++;
                    Log.d(TAG, "horCount++ = " + horCount);
                    usedList.add(wordsCursor.getInt(0));
                    //если прошло успешно, записываем в horCount и в список использованных
                    hasSucceed = true;//конец логики метода
                } else if ((checkLeftResult || checkRightResult
                        || !isLeftDirectionAvailable || !isRightDirectionAvailable)
                        && isTopDirectionAvailable && isBottomDirectionAvailable //если возможно вписать вверх и вниз
                        && !checkTopResult && !checkBottomResult) {//то пытаемся вписать вверх
                    writeTop();
                    writeBottom();
                    verCount++;
                    Log.d(TAG, "verCount++ = " + verCount);
                    usedList.add(wordsCursor.getInt(0));
                    hasSucceed = true;
                    //если в обоих направлениях вписать не удалось
                } else if ((checkBottomResult || checkTopResult
                        || !isBottomDirectionAvailable || !isTopDirectionAvailable)
                        && (checkRightResult || checkLeftResult
                        || !isRightDirectionAvailable || !isLeftDirectionAvailable)) {
                    hasSucceed = false;
                } //дальше конец метода
            }
        }
        return hasSucceed;
    }

    public boolean checkTop() {
        boolean flagTop = false;
        Log.d(TAG, "flagTop = false ");
        Log.d(TAG, "слово для вставки " + currentWord);
        Log.d(TAG, "строка для размещения сверху " + substr1);
        Log.d(TAG, "substr1.length() = " + substr1.length());
        Log.d(TAG, "коорд. appropriateCell по гор " + appropriateCell.getHor() +  ", по верт. " + appropriateCell.getVer());

        for (int i = appropriateCell.getHor() - substr1.length(), k = 0; k < substr1.length(); i++, k++) {
            Log.d(TAG, "входим в цикл для проверки в направлении Top");
            Log.d(TAG, "i = " + i + ", k = " + k);
            letter = Character.toString(substr1.charAt(k)); //получаем букву
            Log.d(TAG, "буква для размещения " + letter);
            for (Cell item: array) {
                if (item.getHor() == i && item.getVer() == appropriateCell.getVer()) {
                    Log.d(TAG, "коорд ячейки по гор: " + i + ", по верт. " + appropriateCell.getVer());
                    if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                        Log.d(TAG, "для буквы " + k + " получаем flagTop = false ");
                    //    break;
                    } else if (item.getLetter().equals(letter)) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        Log.d(TAG, "для буквы " + k + " получаем flagTop = false ");
                    //    break;
                    } else if (!item.getLetter().equals("") && !item.getLetter().equals(letter)) {  //если вставлена другая буква, то стираем записанные буквы
                        //ставим флаг на выход из внешнего цикла
                        flagTop = true;
                        Log.d(TAG, "для буквы " + k + " получаем flagTop = true ");
                        break;
                    }
                }
            }
            if (flagTop) { break ; }
        }
        Log.d(TAG, "вернулся flagTop = " + flagTop);
        return flagTop;
    }

    public void writeTop() {
        for (int i = appropriateCell.getHor() - substr1.length(), k = 0; k < substr1.length(); i++, k++) {
            Log.d(TAG, "входим в цикл для записи в направлении Top");
            letter = Character.toString(substr1.charAt(k)); //получаем букву
            for (Cell item: array) {
                if (item.getHor() == i && item.getVer() == appropriateCell.getVer()) {
                    Log.d(TAG, "записываем в ячейку по гор: " + i + ", по верт. " + appropriateCell.getVer());
                    item.getCellId().setText(letter); // в ячейку
                    item.setLetter(letter);// и в объект
                }
            }
        }
    }

    public boolean checkLeft() {
        boolean flagLeft = false;
        for (int i = appropriateCell.getVer() - substr1.length(), k = 0; k < substr1.length(); i++, k++) {
            letter = Character.toString(substr1.charAt(k)); //получаем букву
            for (Cell item: array) {
                if (item.getHor() == appropriateCell.getHor() && item.getVer() == i) {
                    if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                        //если ячейка пустая, идем проверять следующую
                        break;
                    } else if (item.getLetter().equals(letter)) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        // идем проверять следующую
                        break;
                    } else if (!item.getLetter().equals("") && !item.getLetter().equals(letter)) {  //если вставлена другая буква, то стираем записанные буквы
                        //ставим флаг на выход из внешнего цикла
                        flagLeft = true;
                        break;
                    }
                }
            }
            if (flagLeft) { break ;}
        }
        return flagLeft;
    }

    public void writeLeft() {
        for (int i = appropriateCell.getVer() - substr1.length(), k = 0; k < substr1.length(); i++, k++) {
            letter = Character.toString(substr1.charAt(k)); //получаем букву
            for (Cell item: array) {
                if (item.getHor() == appropriateCell.getHor() && item.getVer() == i) {
                        item.getCellId().setText(letter); // в ячейку
                        item.setLetter(letter);// и в объект
                }
            }
        }
    }

    public boolean checkRight() {
        boolean flagRight = false;
        for (int i = appropriateCell.getVer() + 1, k = 0; k < substr2.length(); i++, k++) {
            letter = Character.toString(substr2.charAt(k)); //получаем букву
            for (Cell item: array) { //находим объект cell с координатами appropriateCell.getHor
                // и i по вертикали
                if (item.getHor() == appropriateCell.getHor() && item.getVer() == i) {
                    if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                        //если ячейка пустая, выходим из цикла
                        break;
                    } else if (item.getLetter().equals(letter)) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        // если совпадают, выходим из цикла
                        break;
                    } else if (!item.getLetter().equals("") && !item.getLetter().equals(letter)) {  //если вставлена другая буква, то стираем записанные буквы
                        //если ячейка занята, выходим из метода и возращаем тру
//ставим флаг на выход из внешнего цикла
                        flagRight = true;
                        break;
                    }
                }
            }
            if (flagRight) { break ;}
        }
        return flagRight;
    }

    public void writeRight() {
        for (int i = appropriateCell.getVer() + 1, k = 0; k < substr2.length(); i++, k++) {
            letter = Character.toString(substr2.charAt(k)); //получаем букву
            for (Cell item: array) { //находим объект cell с координатами appropriateCell.getHor
                // и i по вертикали
                if (item.getHor() == appropriateCell.getHor() && item.getVer() == i) {
                        item.getCellId().setText(letter); // в ячейку
                        item.setLetter(letter);// и в объект
                }
            }
        }
    }

    public boolean checkBottom() {
        boolean flagBottom = false;
        for (int i = appropriateCell.getHor() + 1, k = 0; k < substr2.length(); i++, k++) {
            letter = Character.toString(substr2.charAt(k)); //получаем букву
            for (Cell item: array) { //находим объект cell с координатами appropriateCell.getVer
                // и i по горизонтали
                if (item.getHor() == i && item.getVer() == appropriateCell.getVer()) {
                    if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                        //если ячейка пустая, выходим из внутреннего цикла и проверяем дальше
                        break;
                    } else if (item.getLetter().equals(letter)) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        // то выходим из цикла и проверяем дальше
                        break;
                    } else if (!item.getLetter().equals("") && !item.getLetter().equals(letter)) {
                        //ставим флаг на выход из внешнего цикла
                        flagBottom = true;
                        break;
                    }
                }
            }
            if (flagBottom) { break ;}
        }
        return flagBottom;
    }

    public void writeBottom() {
        for (int i = appropriateCell.getHor() + 1, k = 0; k < substr2.length(); i++, k++) {
            letter = Character.toString(substr2.charAt(k)); //получаем букву
            for (Cell item: array) { //находим объект cell с координатами appropriateCell.getVer
                // и i по горизонтали
                if (item.getHor() == i && item.getVer() == appropriateCell.getVer()) {
                    item.getCellId().setText(letter); // в ячейку
                    item.setLetter(letter);// и в объект
                }
            }
        }
    }

    public boolean checkWordWithoutAppropriate() {
        boolean flagWithoutAppropriate = false;
        Log.d(TAG, "checkWordWithoutAppropriate() ");
        if (direction == 1) { //для гориз.ориентации
            for (int i = ver, k = 0; k < currentWord.length(); i++, k++) {
                letter = Character.toString(currentWord.charAt(k)); //получаем букву
                for (Cell item: array) { //находим объект cell с координатами i и ver
                    if (item.getVer() == i && item.getHor() == hor) {
                        if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                            Log.d(TAG, "пустая ячйека по горизонтали ");
                            break; //выходим из цикла и переходим к следующей ячейке
                        } else if (item.getLetter().equals(letter)) {
                            //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                            // то переходим к следующей ячейке на пути - выходим из цикла
                            Log.d(TAG, "пропустили совпадающую букву по горизонтали " + letter);
                                break;
                        } else {  //если вставлена другая буква, то отдаем флаг
                            flagWithoutAppropriate = true;
                            Log.d(TAG, " flag = true ");
                        }
                    }
                }
                if (flagWithoutAppropriate) { break; }
            }
        }

        if (direction == 2) {//для верт.ориентации
            for (int i = hor, k = 0; k < currentWord.length(); i++, k++) {
                letter = Character.toString(currentWord.charAt(k)); //получаем букву
                for (Cell item: array) { //находим объект cell с координатами hor и i
                    if (item.getHor() == i && item.getVer() == ver) {
                        if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                            Log.d(TAG, "разметсили букву по вертикали " + letter);
                            Log.d(TAG, "hor = " + i);
                            Log.d(TAG, "ver = " + item.getHor());
                            break; //выходим из цикла и переходим к следующей ячейке
                        } else if (item.getLetter().equals(letter)) {
                            Log.d(TAG, "буквы совпадают " + letter);
                            //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                            // то переходим к следующей ячейке на пути - выходим из цикла
                            break;
                        } else {  //если вставлена другая буква, то стираем записанные буквы
                            flagWithoutAppropriate = true;
                            Log.d(TAG, " flag = true ");
                        }
                    }
                }
            if (flagWithoutAppropriate) { break; }
            }
        }
        return flagWithoutAppropriate;
    }

    public void writeWordWithoutAppropriate() {
        boolean flagWithoutAppropriate = false;
        Log.d(TAG, "writeWordWithoutAppropriate() ");
        if (direction == 1) { //для гориз.ориентации
            for (int i = ver, k = 0; k < currentWord.length(); i++, k++) {
                letter = Character.toString(currentWord.charAt(k)); //получаем букву
                for (Cell item: array) { //находим объект cell с координатами i и ver
                    if (item.getHor() == hor && item.getVer() == i) {
                        item.getCellId().setText(letter);
                        item.setLetter(letter);
                        Log.d(TAG, "разметсили букву по горизонтали " + letter);
                        Log.d(TAG, "hor = " + item.getHor());
                        Log.d(TAG, "ver = " + i);
                    }
                }
            }
        }

        if (direction == 2) {//для верт.ориентации
            for (int i = hor, k = 0; k < currentWord.length(); i++, k++) {
                letter = Character.toString(currentWord.charAt(k)); //получаем букву
                for (Cell item: array) { //находим объект cell с координатами hor и i
                    if (item.getHor() == i && item.getVer() == ver) {
                            item.getCellId().setText(letter);
                            item.setLetter(letter);
                            Log.d(TAG, "разметсили букву по вертикали " + letter);
                            Log.d(TAG, "hor = " + i);
                            Log.d(TAG, "ver = " + item.getHor());
                    }
                }
            }
        }

        // в конце записываем в коллекцию использованных

        usedList.add(wordsCursor.getInt(0));
            // и в количество горизонтально или вертикально расположенных слов
        if (direction == 1) {
            horCount++;
            Log.d(TAG, "horCount++ = " + horCount);
        } else if (direction == 2) {
            verCount++;
            Log.d(TAG, "verCount++ = " + verCount);
        }

    }

    public void getRandomWord () {
        do  {wordsCursor.moveToPosition(r.nextInt(linesCount));
            checkUsed();
            currentWord = wordsCursor.getString(1);
        } while (isUsed ||
                currentWord.length() > CELLS_AMOUNT ||
                currentWord.length() < LIMIT ||
                currentWord.contains(" "));
        /*добавить вырезание 1 слова из предложения, добавить проверку, что не содержит цифр и
        др. символов*/
        currentWord = currentWord.toUpperCase(); //во избежание путаницы используем всегда заглавные буквы
    }

    public void getRandomPosition () {
        // генерируем координаты для первой буквы
        if (direction == 1) { //если ориент. гориз.
            hor = r.nextInt(CELLS_AMOUNT) + 1; // по горизонтали любое значение
            ver = r.nextInt(CELLS_AMOUNT - currentWord.length()) + 1; // генерируем
            // значение в таком диапазоне, чтобы слово не вышло за пределы поля
        }

        if (direction == 2) { // если слово располагаем по вертикали, тогда наоборот
            hor = r.nextInt(CELLS_AMOUNT - currentWord.length()) + 1;
            ver = r.nextInt(CELLS_AMOUNT) + 1; // по вертикали любое значение
        }
    }

    public void placeFirstWord() {
        //первое слово всегда в горизонтальной ориентации
        direction = 1;
        //цикл, который прописывает букву в таблицу и сохраняет ее в параметры объекта
        for (int i = ver, k = 0; k < currentWord.length(); i++, k++) {
            letter = Character.toString(currentWord.charAt(k)); //получаем букву
            for (Cell item: array) { //находим объект cell с координатами i и ver
                if (item.getHor() == hor && item.getVer() == i) {
                    item.getCellId().setText(letter); //вставляем букву в ячейку
                    item.setLetter(letter); //и в объект
                    break;
                }
            }
        }
        //в конце записываем в коллекцию использованных
        usedList.add(wordsCursor.getInt(0));
        // и в количество горизонтально расположенных слов
        horCount++;
        Log.d(TAG, "Разместили первое слово " + currentWord);
        Log.d(TAG, "horCount++ = " + horCount);
    }

    public void checkUsed() {
        isUsed = false;
        for (int i = 0, usedSize = usedList.size(); i < usedSize; i++) {
            Integer integer = usedList.get(i);
            if (integer == wordsCursor.getInt(0)) {
                isUsed = true;
                break;
            }
        }
    }

    public void clearVariables() {
        currentWord = "";
        letter = "";
        substr1 = "";
        substr2 = "";
        hor = 0;
        ver = 0;
        appropriateCellsList.clear();
       hasSucceed = false;
       isUsed = false;
       isTheSame = false;
    }

    public void createArray() {
        array[0] = new Cell(1, 1, binding.h1v1, "");
        array[1] = new Cell(1, 2, binding.h1v2, "");
        array[2] = new Cell(1, 3, binding.h1v3, "");
        array[3] = new Cell(1, 4, binding.h1v4, "");
        array[4] = new Cell(1, 5, binding.h1v5, "");
        array[5] = new Cell(1, 6, binding.h1v6, "");
        array[6] = new Cell(1, 7, binding.h1v7, "");
        array[7] = new Cell(1, 8, binding.h1v8, "");
        array[8] = new Cell(1, 9, binding.h1v9, "");
        array[9] = new Cell(1, 10, binding.h1v10, "");
        array[10] = new Cell(2, 1, binding.h2v1, "");
        array[11] = new Cell(2, 2, binding.h2v2, "");
        array[12] = new Cell(2, 3, binding.h2v3, "");
        array[13] = new Cell(2, 4, binding.h2v4, "");
        array[14] = new Cell(2, 5, binding.h2v5, "");
        array[15] = new Cell(2, 6, binding.h2v6, "");
        array[16] = new Cell(2, 7, binding.h2v7, "");
        array[17] = new Cell(2, 8, binding.h2v8, "");
        array[18] = new Cell(2, 9, binding.h2v9, "");
        array[19] = new Cell(2, 10, binding.h2v10, "");
        array[20] = new Cell(3, 1, binding.h3v1, "");
        array[21] = new Cell(3, 2, binding.h3v2, "");
        array[22] = new Cell(3, 3, binding.h3v3, "");
        array[23] = new Cell(3, 4, binding.h3v4, "");
        array[24] = new Cell(3, 5, binding.h3v5, "");
        array[25] = new Cell(3, 6, binding.h3v6, "");
        array[26] = new Cell(3, 7, binding.h3v7, "");
        array[27] = new Cell(3, 8, binding.h3v8, "");
        array[28] = new Cell(3, 9, binding.h3v9, "");
        array[29] = new Cell(3, 10, binding.h3v10, "");
        array[30] = new Cell(4, 1, binding.h4v1, "");
        array[31] = new Cell(4, 2, binding.h4v2, "");
        array[32] = new Cell(4, 3, binding.h4v3, "");
        array[33] = new Cell(4, 4, binding.h4v4, "");
        array[34] = new Cell(4, 5, binding.h4v5, "");
        array[35] = new Cell(4, 6, binding.h4v6, "");
        array[36] = new Cell(4, 7, binding.h4v7, "");
        array[37] = new Cell(4, 8, binding.h4v8, "");
        array[38] = new Cell(4, 9, binding.h4v9, "");
        array[39] = new Cell(4, 10, binding.h4v10, "");
        array[40] = new Cell(5, 1, binding.h5v1, "");
        array[41] = new Cell(5, 2, binding.h5v2, "");
        array[42] = new Cell(5, 3, binding.h5v3, "");
        array[43] = new Cell(5, 4, binding.h5v4, "");
        array[44] = new Cell(5, 5, binding.h5v5, "");
        array[45] = new Cell(5, 6, binding.h5v6, "");
        array[46] = new Cell(5, 7, binding.h5v7, "");
        array[47] = new Cell(5, 8, binding.h5v8, "");
        array[48] = new Cell(5, 9, binding.h5v9, "");
        array[49] = new Cell(5, 10, binding.h5v10, "");
        array[50] = new Cell(6, 1, binding.h6v1, "");
        array[51] = new Cell(6, 2, binding.h6v2, "");
        array[52] = new Cell(6, 3, binding.h6v3, "");
        array[53] = new Cell(6, 4, binding.h6v4, "");
        array[54] = new Cell(6, 5, binding.h6v5, "");
        array[55] = new Cell(6, 6, binding.h6v6, "");
        array[56] = new Cell(6, 7, binding.h6v7, "");
        array[57] = new Cell(6, 8, binding.h6v8, "");
        array[58] = new Cell(6, 9, binding.h6v9, "");
        array[59] = new Cell(6, 10, binding.h6v10, "");
        array[60] = new Cell(7, 1, binding.h7v1, "");
        array[61] = new Cell(7, 2, binding.h7v2, "");
        array[62] = new Cell(7, 3, binding.h7v3, "");
        array[63] = new Cell(7, 4, binding.h7v4, "");
        array[64] = new Cell(7, 5, binding.h7v5, "");
        array[65] = new Cell(7, 6, binding.h7v6, "");
        array[66] = new Cell(7, 7, binding.h7v7, "");
        array[67] = new Cell(7, 8, binding.h7v8, "");
        array[68] = new Cell(7, 9, binding.h7v9, "");
        array[69] = new Cell(7, 10, binding.h7v10, "");
        array[70] = new Cell(8, 1, binding.h8v1, "");
        array[71] = new Cell(8, 2, binding.h8v2, "");
        array[72] = new Cell(8, 3, binding.h8v3, "");
        array[73] = new Cell(8, 4, binding.h8v4, "");
        array[74] = new Cell(8, 5, binding.h8v5, "");
        array[75] = new Cell(8, 6, binding.h8v6, "");
        array[76] = new Cell(8, 7, binding.h8v7, "");
        array[77] = new Cell(8, 8, binding.h8v8, "");
        array[78] = new Cell(8, 9, binding.h8v9, "");
        array[79] = new Cell(8, 10, binding.h8v10, "");
        array[80] = new Cell(9, 1, binding.h9v1, "");
        array[81] = new Cell(9, 2, binding.h9v2, "");
        array[82] = new Cell(9, 3, binding.h9v3, "");
        array[83] = new Cell(9, 4, binding.h9v4, "");
        array[84] = new Cell(9, 5, binding.h9v5, "");
        array[85] = new Cell(9, 6, binding.h9v6, "");
        array[86] = new Cell(9, 7, binding.h9v7, "");
        array[87] = new Cell(9, 8, binding.h9v8, "");
        array[88] = new Cell(9, 9, binding.h9v9, "");
        array[89] = new Cell(9, 10, binding.h9v10, "");
        array[90] = new Cell(10, 1, binding.h10v1, "");
        array[91] = new Cell(10, 2, binding.h10v2, "");
        array[92] = new Cell(10, 3, binding.h10v3, "");
        array[93] = new Cell(10, 4, binding.h10v4, "");
        array[94] = new Cell(10, 5, binding.h10v5, "");
        array[95] = new Cell(10, 6, binding.h10v6, "");
        array[96] = new Cell(10, 7, binding.h10v7, "");
        array[97] = new Cell(10, 8, binding.h10v8, "");
        array[98] = new Cell(10, 9, binding.h10v9, "");
        array[99] = new Cell(10, 10, binding.h10v10, "");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        db.close();
    //    if (db != null) {
    //        db.close();
    //    }
    }
}