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
import android.widget.TextView;
import android.widget.Toast;

import com.example.sqliteapp.databinding.FragmentWordSearchBinding;

import java.util.ArrayList;
import java.util.Random;

public class WordSearchFragment extends Fragment {

    private FragmentWordSearchBinding binding;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    final int CELLS_AMOUNT = 10; // количество ячеек по горизонтали и по вертикали
    final int LIMIT = 4; // минимальная длина слова
    ArrayList<Integer> usedList = new ArrayList<Integer>(); //коллекция для хранения уже использованных слов
    boolean isUsed;
    String currentWord;
    Random r = new Random(); //объект для генерации рандомных чисел
    int ver, hor, direction;
    String cellId;
    char letter;
    TextView h1v1;
    Cell[] array = new Cell[100];
    // Cell cell = new Cell();
    ArrayList<Cell> cellList = new ArrayList<Cell>(); //коллекция для хранения созданных объектов cell
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
        h1v1 = view.findViewById(R.id.h1v1);
    }

    @Override
    public void onResume() {
        super.onResume();
        db = databaseHelper.open();
        wordsCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE
        // нужно ли исключать слова?      +  " WHERE study = 1"
                , null);
        wordsCursor.moveToFirst();
        int linesCount = wordsCursor.getCount();
    /* ...проверка, что в бд не менее 100 слов*/

        // получаем рандомное слово
        //функция
        // public void getRandomWord () {}
        do  {
            wordsCursor.moveToPosition(r.nextInt(linesCount));
            checkUsed();
            currentWord = wordsCursor.getString(1);
        } while (isUsed ||
                currentWord.length() > CELLS_AMOUNT ||
                currentWord.length() < LIMIT ||
                currentWord.contains(" "));

                placeFirstWord();
    }


    public void placeFirstWord() {
        //записываем в коллекцию использованных
        usedList.add(wordsCursor.getInt(0));
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

        cellId = "h" + hor + "v" + ver;
        Toast.makeText(getActivity(), cellId.toString(),Toast.LENGTH_LONG).show();

        //делаем цикл
        if (direction == 1) {
            for (int i = hor, j = ver, k = 0; i < hor + currentWord.length(); i++, k++) { //не
                // прописываем k, потому что и так понятно
                cellId = "tv" + hor  + ver;
                letter = currentWord.charAt(k);
                View view = view.findViewById(R.id.cellId.toString());
                binding.cellId.setText(letter);
                Cell cell = new Cell(hor, ver, direction, cellId, letter);
                cellList.add(cell);
            }
        }
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