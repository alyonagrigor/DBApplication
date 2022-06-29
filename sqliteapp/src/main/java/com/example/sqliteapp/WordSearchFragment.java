package com.example.sqliteapp;

/**добавить проверку
 * увеличить до 16 на 16
 * сгенерировать рандомное положение 3 значения
 * убедиться, что слово поместится
 * разбить слово посимвольно и разместить
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.sqliteapp.databinding.FragmentWordSearchBinding;
import java.util.ArrayList;
import java.util.Random;

public class WordSearchFragment extends Fragment {

    private FragmentWordSearchBinding binding;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    int linesCount, ver, hor, direction, horCount = 0, verCount = 0;
    final int CELLS_AMOUNT = 10; // количество ячеек по горизонтали и по вертикали
    final int LIMIT = 4; // минимальная длина слова
    boolean isUsed = false;
    String currentWord, cellId, letter;
    Random r = new Random(); //объект для генерации рандомных чисел
    ArrayList<Integer> usedList = new ArrayList<Integer>(); //коллекция для хранения уже
    // использованных слов по айди в бд
    Cell[] array = new Cell[CELLS_AMOUNT*CELLS_AMOUNT];

    public WordSearchFragment() {
         }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
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

        //заполняем массив объектами cell по числу ячеек
        array[0] = new Cell (1, 1, binding.h1v1, "0");
        array[1] = new Cell (1, 2, binding.h1v2, "0");
        array[2] = new Cell (1, 3, binding.h1v3, "0");
        array[3] = new Cell (1, 4, binding.h1v4, "0");
        array[4] = new Cell (1, 5, binding.h1v5, "0");
        array[5] = new Cell (1, 6, binding.h1v6, "0");
        array[6] = new Cell (1, 7, binding.h1v7, "0");
        array[7] = new Cell (1, 8, binding.h1v8, "0");
        array[8] = new Cell (1, 9, binding.h1v9, "0");
        array[9] = new Cell (1, 10, binding.h1v10, "0");
        array[10] = new Cell (2, 1, binding.h2v1, "0");
        array[11] = new Cell (2, 2, binding.h2v2, "0");
        array[12] = new Cell (2, 3, binding.h2v3, "0");
        array[13] = new Cell (2, 4, binding.h2v4, "0");
        array[14] = new Cell (2, 5, binding.h2v5, "0");
        array[15] = new Cell (2, 6, binding.h2v6, "0");
        array[16] = new Cell (2, 7, binding.h2v7, "0");
        array[17] = new Cell (2, 8, binding.h2v8, "0");
        array[18] = new Cell (2, 9, binding.h2v9, "0");
        array[19] = new Cell (2, 10, binding.h2v10, "0");
        array[20] = new Cell (3, 1, binding.h3v1, "0");
        array[21] = new Cell (3, 2, binding.h3v2, "0");
        array[22] = new Cell (3, 3, binding.h3v3, "0");
        array[23] = new Cell (3, 4, binding.h3v4, "0");
        array[24] = new Cell (3, 5, binding.h3v5, "0");
        array[25] = new Cell (3, 6, binding.h3v6, "0");
        array[26] = new Cell (3, 7, binding.h3v7, "0");
        array[27] = new Cell (3, 8, binding.h3v8, "0");
        array[28] = new Cell (3, 9, binding.h3v9, "0");
        array[29] = new Cell (3, 10, binding.h3v10, "0");
        array[30] = new Cell (4, 1, binding.h4v1, "0");
        array[31] = new Cell (4, 2, binding.h4v2, "0");
        array[32] = new Cell (4, 3, binding.h4v3, "0");
        array[33] = new Cell (4, 4, binding.h4v4, "0");
        array[34] = new Cell (4, 5, binding.h4v5, "0");
        array[35] = new Cell (4, 6, binding.h4v6, "0");
        array[36] = new Cell (4, 7, binding.h4v7, "0");
        array[37] = new Cell (4, 8, binding.h4v8, "0");
        array[38] = new Cell (4, 9, binding.h4v9, "0");
        array[39] = new Cell (4, 10, binding.h4v10, "0");
        array[40] = new Cell (5, 1, binding.h5v1, "0");
        array[41] = new Cell (5, 2, binding.h5v2, "0");
        array[42] = new Cell (5, 3, binding.h5v3, "0");
        array[43] = new Cell (5, 4, binding.h5v4, "0");
        array[44] = new Cell (5, 5, binding.h5v5, "0");
        array[45] = new Cell (5, 6, binding.h5v6, "0");
        array[46] = new Cell (5, 7, binding.h5v7, "0");
        array[47] = new Cell (5, 8, binding.h5v8, "0");
        array[48] = new Cell (5, 9, binding.h5v9, "0");
        array[49] = new Cell (5, 10, binding.h5v10, "0");
        array[50] = new Cell (6, 1, binding.h6v1, "0");
        array[51] = new Cell (6, 2, binding.h6v2, "0");
        array[52] = new Cell (6, 3, binding.h6v3, "0");
        array[53] = new Cell (6, 4, binding.h6v4, "0");
        array[54] = new Cell (6, 5, binding.h6v5, "0");
        array[55] = new Cell (6, 6, binding.h6v6, "0");
        array[56] = new Cell (6, 7, binding.h6v7, "0");
        array[57] = new Cell (6, 8, binding.h6v8, "0");
        array[58] = new Cell (6, 9, binding.h6v9, "0");
        array[59] = new Cell (6, 10, binding.h6v10, "0");
        array[60] = new Cell (7, 1, binding.h7v1, "0");
        array[61] = new Cell (7, 2, binding.h7v2, "0");
        array[62] = new Cell (7, 3, binding.h7v3, "0");
        array[63] = new Cell (7, 4, binding.h7v4, "0");
        array[64] = new Cell (7, 5, binding.h7v5, "0");
        array[65] = new Cell (7, 6, binding.h7v6, "0");
        array[66] = new Cell (7, 7, binding.h7v7, "0");
        array[67] = new Cell (7, 8, binding.h7v8, "0");
        array[68] = new Cell (7, 9, binding.h7v9, "0");
        array[69] = new Cell (7, 10, binding.h7v10, "0");
        array[70] = new Cell (8, 1, binding.h8v1, "0");
        array[71] = new Cell (8, 2, binding.h8v2, "0");
        array[72] = new Cell (8, 3, binding.h8v3, "0");
        array[73] = new Cell (8, 4, binding.h8v4, "0");
        array[74] = new Cell (8, 5, binding.h8v5, "0");
        array[75] = new Cell (8, 6, binding.h8v6, "0");
        array[76] = new Cell (8, 7, binding.h8v7, "0");
        array[77] = new Cell (8, 8, binding.h8v8, "0");
        array[78] = new Cell (8, 9, binding.h8v9, "0");
        array[79] = new Cell (8, 10, binding.h8v10, "0");
        array[80] = new Cell (9, 1, binding.h9v1, "0");
        array[81] = new Cell (9, 2, binding.h9v2, "0");
        array[82] = new Cell (9, 3, binding.h9v3, "0");
        array[83] = new Cell (9, 4, binding.h9v4, "0");
        array[84] = new Cell (9, 5, binding.h9v5, "0");
        array[85] = new Cell (9, 6, binding.h9v6, "0");
        array[86] = new Cell (9, 7, binding.h9v7, "0");
        array[87] = new Cell (9, 8, binding.h9v8, "0");
        array[88] = new Cell (9, 9, binding.h9v9, "0");
        array[89] = new Cell (9, 10, binding.h9v10, "0");
        array[90] = new Cell (10, 1, binding.h10v1, "0");
        array[91] = new Cell (10, 2, binding.h10v2, "0");
        array[92] = new Cell (10, 3, binding.h10v3, "0");
        array[93] = new Cell (10, 4, binding.h10v4, "0");
        array[94] = new Cell (10, 5, binding.h10v5, "0");
        array[95] = new Cell (10, 6, binding.h10v6, "0");
        array[96] = new Cell (10, 7, binding.h10v7, "0");
        array[97] = new Cell (10, 8, binding.h10v8, "0");
        array[98] = new Cell (10, 9, binding.h10v9, "0");
        array[99] = new Cell (10, 10, binding.h10v10, "0");
    }

    @Override
    public void onResume() {
        super.onResume();
        db = databaseHelper.open();
        wordsCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE
        // нужно ли исключать слова?      +  " WHERE study = 1"
                , null);
        wordsCursor.moveToFirst();
        linesCount = wordsCursor.getCount();
    /* ...проверка, что в бд не менее 100 слов*/

        // получаем рандомное слово
        getRandomWord();
        //генерируем рандомную позицию
        getRandomPosition();
        //вставляем первое слово
         placeFirstWord();
        /* БОЛЬШОЙ ЦИКЛ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */
       // while (какие условия?)  {
            //обнуляем переменные
            currentWord = "";
            letter = "";
            hor = 0;
            ver = 0;
            direction = 0;
            // получаем рандомное слово
            getRandomWord();
            //генерируем рандомную позицию
            getRandomPosition();

      //  }
        /* КОНЕЦ ЦИКЛА!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

        //в конце выводим количество слов по гор. и вертикали в textView
        binding.wsText.setText(getString(R.string.findWords, horCount, verCount));
    }

    public void getRandomWord () {
        do  {
            wordsCursor.moveToPosition(r.nextInt(linesCount));
            checkUsed();
            currentWord = wordsCursor.getString(1);
        } while (isUsed ||
                currentWord.length() > CELLS_AMOUNT ||
                currentWord.length() < LIMIT ||
                currentWord.contains(" "));
    }

    public void getRandomPosition () {
        //генерируем гориз или верт ориентацию
        direction = r.nextInt(2) + 1 ; //получим 1 или 2 - гор или верт
        // генерируем координаты для первой буквы
        if (direction == 1) { //если ориент. гориз.
            hor = r.nextInt(CELLS_AMOUNT - currentWord.length()) + 1; // генерируем
            // значение в таком диапазоне, чтобы слово точно поместилось, например, для слова из
            // пяти букв это будет от 1 до 6
            ver = r.nextInt(CELLS_AMOUNT) + 1; // по вертикали любое значение
        } else { // если слово располагаем по вертикали, тогда наоборот
            hor = r.nextInt(CELLS_AMOUNT) + 1; // по горизонтали любое значение
            ver = r.nextInt(CELLS_AMOUNT - currentWord.length()) + 1;
        }

    }

    public void placeFirstWord() {
        //цикл, который прописывает букву в таблицу и сохраняет ее в параметры объекта
        if (direction == 1) { //для гориз.ориентации
            for (int i = hor, k = 0; k < currentWord.length(); i++, k++) {
                letter = Character.toString(currentWord.charAt(k)); //получаем букву
                for (Cell num : array) { //находим объект cell с координатами i и ver
                    if (num.getHor() == i && num.getVer() == ver) {
                        num.getCellId().setText(letter);
                        break;
                    }
                }
            }
        }

        if (direction == 2) {//для верт.ориентации
            for (int j = ver, k = 0; k < currentWord.length(); j++, k++) {
                letter = Character.toString(currentWord.charAt(k)); //получаем букву
                for (Cell num : array) { //находим объект cell с координатами hor и j
                    if (num.getHor() == hor && num.getVer() == j) {
                        num.getCellId().setText(letter);
                    }
                }
            }
        }

        //в конце записываем в коллекцию использованных
        usedList.add(wordsCursor.getInt(0));
        // и в количество горизонтально или вертикально расположенных слов
        if (direction == 1) { horCount++; }
        else { verCount++; }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        if (db != null) {
            db.close();
        }
    }
}