package com.example.sqliteapp;

/**
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
    int linesCount = 0, ver = 0, hor = 0, horCount = 0, verCount = 0, direction = 1;
    final int CELLS_AMOUNT = 10; // количество ячеек по горизонтали и по вертикали
    final int LIMIT = 3; // минимальная длина слова
    final int TOTAL_TRIES_AMOUNT = 50; //сколько раз нужно прогнать главный цикл после вставки
    // первого слова
    boolean isUsed = false;
    String currentWord, letter, substr1, substr2;
    Random r = new Random(); //объект для генерации рандомных чисел
    Cell[] array = new Cell[CELLS_AMOUNT * CELLS_AMOUNT]; //массив для хранения ячеек
    Cell appropriateCell;
    boolean flagTop = false, flagBottom = false, flagLeft = false, flagRight = false; //флаги для
    //функций write
    ArrayList<Integer> usedList = new ArrayList<Integer>(); //коллекция для хранения уже
    // использованных слов по айди в бд
    ArrayList<TextView> CurrentWordCells = new ArrayList<TextView>(); //коллекция для записи,
    // в какие ячейки вписаны буквы текущего слова, чтобы можно было отменить
    ArrayList<Cell> currentWordCellsTop = new ArrayList<Cell>(); //аналогичная коллекция
    //для записи при вставке методом writeTop()
    ArrayList<Cell> currentWordCellsBottom = new ArrayList<Cell>(); //аналогичная коллекция
    //для записи при вставке методом writeBottom()
    ArrayList<Cell> currentWordCellsLeft = new ArrayList<Cell>(); //аналогичная коллекция
    //для записи при вставке методом writeLeft()
    ArrayList<Cell> currentWordCellsRight = new ArrayList<Cell>(); //аналогичная коллекция
    //для записи при вставке методом writeRight()

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

        //заполняем массив объектами cell по числу ячеек
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

        /* БОЛЬШОЙ ЦИКЛ ДЛЯ ДОБАВЛЕНИЯ ДАЛЬНЕЙШИХ СЛОВ ПОСЛЕ ПЕРВОГО !!!!!!!!!!!!!!!!!!!!!!!!!!!
         безопасный сценарий - цикл не зависит от переменных, а просто прогоняется
         TOTAL_TRIES_AMOUNT раз, в какие-то
         из этих TOTAL_TRIES_AMOUNT раз слово будет вставлено, а в какие-то - нет */
        for (int n = 0; n < TOTAL_TRIES_AMOUNT; n++) {
            //обнуляем переменные
        //    flagSubstr2 = false;
        //    flagSubstr1 = false;
            currentWord = "";
            letter = "";
            substr1 = "";
            substr2 = "";
            hor = 0;
            ver = 0;
            CurrentWordCells.clear();
            appropriateCell = null;
            // получаем рандомное слово
            getRandomWord();

            //подбираем ячейку, в которой вставлена буква, совпадающая с буквой в новом слове -
            //объект appropriateCell
            findAppropriateCell();
            //получаем (или не получаем) объект appropriateCell
            if (appropriateCell == null) {
                //-------------ВЕРОЯТНОСТЬ ЭТОГО СЦЕНАРИЯ ПРАКТИЧЕСКИ РАВНА НУЛЮ
                //-------------СКОРЕЕ ВСЕГО ЭТО БУДЕТ ВТОРОЕ СЛОВО
                // протестирована корректная работа с 4 словами
                //если нет совпадающих букв, ставим в любое другое место
                //не меняем ориентацию, чтобы расположить слово параллельно предыдущему, так как скорее
                // всего это будет второе слово, с которым у первого нет совпадающих букв
                getRandomPosition();
                placeWordWithoutAppropriate();
                //--------------КОНЕЦ МАЛОВЕРОЯТНОГО СЦЕНАРИЯ


            } else { //если есть совпадающие буквы (appropriateCell == !null)
                placeWordWithAppropriate();
                //если с первой попытки вставить не удалось, то пробуем подобрать другую
                // appropriateCell 100 раз
                for (int y = 0; y < 100; y++) {
                    findAppropriateCell();
                    if (appropriateCell != null) {
                        break;
                    }
                }
                // если удалось подобрать appropriateCell, то пытаемся вставить 1 раз
                if (appropriateCell != null) {
                    placeWordWithAppropriate();
                }
                //иначе выходим запускаем цикл снова

            }
        }
        /* КОНЕЦ ЦИКЛА!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */


        //в конце выводим количество слов по гор. и вертикали в textView
        binding.wsText.setText(getString(R.string.findWords, horCount, verCount));

        //и заполняем оставшиеся ячейки случайными буквами

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

    public void placeWordWithAppropriate() {
        //разрезаем слово на 2 подстроки, до и после совпадающей буквы, не включая эту букву;
        //если сопадает первая или последняя буква, то первая или вторая подстрока может оказаться
        // пустой, в этом случае мы явно прописываем подстроки как пустые
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

        //проверяем, доступны ли ячейки вокруг appropriateCell: 1. должна быть пустая строка
        //2. не должна находиться за пределами поля
        //инициализируем ячейки

        Cell rightCell = new Cell(0, 0, null, ""), // создаем произвольный объект, чтобы избежать NullException
            leftCell = new Cell(0, 0, null, ""),
            topCell = new Cell(0, 0, null, ""),
            bottomCell = new Cell(0, 0, null, "");

        //назначаем булевы переменные для информации о том, не находится ли ячейка за пределами
        // поля, изначально false
        boolean isRightCellOutOfBorder = false,
                isLeftCellOutOfBorder = false,
                isTopCellOutOfBorder = false,
                isBottomCellOutOfBorder = false;

        //сразу вычисляем координаты ячейки и назначаем булевой значение false, если они выпадают
        //за пределы поля, только после проверки этого условия присваиваем ячейке ссылку на объект
        for (Cell item: array) {
            if (appropriateCell.getVer() + 1 > CELLS_AMOUNT) {
                isRightCellOutOfBorder = true;
            } else if (item.getHor() == appropriateCell.getHor()
                    && item.getVer() == appropriateCell.getVer() + 1) {
                rightCell = item;
            }
        }

        for (Cell item: array) {
            if (appropriateCell.getVer() + 1 < 1) {
                isLeftCellOutOfBorder = true;
            } else if (item.getHor() == appropriateCell.getHor() && item.getVer() == appropriateCell.getVer() - 1) {
                leftCell = item;
            }
        }

        for (Cell item: array) {
            if (appropriateCell.getHor() - 1 < 1) {
                isTopCellOutOfBorder = true;
            } else if (item.getHor() == appropriateCell.getHor() - 1 && item.getVer() == appropriateCell.getVer()) {
                topCell = item;
            }
        }

        for (Cell item: array) {
            if (appropriateCell.getHor() + 1 > CELLS_AMOUNT) {
                isBottomCellOutOfBorder = true;
            } else if (item.getHor() == appropriateCell.getHor() + 1 && item.getVer() == appropriateCell.getVer()) {
                bottomCell = item;
            }
        }

        //назначаем булевы переменные для информации о том, пустая ли ячейка и проверяем
        boolean isRightCellEmpty = rightCell.getLetter().equals("");
        boolean isLeftCellEmpty = leftCell.getLetter().equals("");
        boolean isTopCellEmpty = topCell.getLetter().equals("");
        boolean isBottomCellEmpty = bottomCell.getLetter().equals("");


        //назначаем булевы переменные для информации о том, доступна ли ячейка для записи
        boolean isRightCellAvailable = false,
                isLeftCellAvailable = false,
                isTopCellAvailable = false,
                isBottomCellAvailable = false;

        // и наконец вычисляем это:
        if (!isRightCellOutOfBorder && isRightCellEmpty) {
            isRightCellAvailable = true;
        } //во всех прочих случаях остается false

        if (!isLeftCellOutOfBorder && isLeftCellEmpty) {
            isLeftCellAvailable = true;
        } //во всех прочих случаях остается false

        if (!isTopCellOutOfBorder && isTopCellEmpty) {
            isTopCellAvailable = true;
        } //во всех прочих случаях остается false

        if (!isBottomCellOutOfBorder && isBottomCellEmpty) {
            isBottomCellAvailable = true;
        } //во всех прочих случаях остается false


        //на этом шаге мы можем сразу выйти из метода, если нужные ячейки недоступны
        if (substr1.equals("") && !substr2.equals("")) { //если пустая substr1
            //то проверяем только ячейки справа и снизу, куда нужно вписать substr1
            if (!isRightCellAvailable && !isBottomCellAvailable) {
                return;
            }

        } else if (!substr1.equals("") && substr2.equals("")) { //если пустая substr2
            if (!isLeftCellAvailable && !isTopCellAvailable) {
                return;
            }

        } else { //если обе подстроки не пустые
            if (!isLeftCellAvailable || !isRightCellAvailable
                    && !isTopCellAvailable || !isBottomCellAvailable) {
                return;
            } //если
            // недоступна одна ячейка слева или справа, то мы не сможем ничего вписать по горизонтали
            //аналогично по вертикали
        }

        //далее проверяем, попадет ли первая или последняя буква слова за границы поля
        //получаем координаты ячеек для первой и последней буквы
        //откладываем длину substr1 влево и вверх
        boolean isTopDirectionAvailable = false,
                isLeftDirectionAvailable = false,
                isBottomDirectionAvailable = false,
                isRightDirectionAvailable = false;

        if (!substr1.equals("")) {
            //проверяем верхнее направление
            isTopDirectionAvailable = appropriateCell.getHor() - substr1.length() > 0;
            //левое направление
            isLeftDirectionAvailable = appropriateCell.getVer() - substr1.length() > 0;
        }
        if (!substr2.equals("")) {//substr2
            //проверяем нижнее направление
            isBottomDirectionAvailable = appropriateCell.getHor() + substr2.length() < CELLS_AMOUNT + 1;
            //правое направление
            isRightDirectionAvailable = appropriateCell.getVer() + substr2.length() < CELLS_AMOUNT + 1;
        }

        //на этом этапе также можем сразу выйти из метода, если нужные направления недоступны
        if (substr1.equals("") && !substr2.equals("")) { //если пустая substr1
            //то проверяем только направления справа и снизу, куда нужно вписать substr1
            if (!isRightDirectionAvailable && !isBottomDirectionAvailable) {
                return;
            }

        } else if (!substr1.equals("") && substr2.equals("")) { //если пустая substr2
            if (!isLeftDirectionAvailable && !isTopDirectionAvailable) {
                return;
            }

        } else { //если обе подстроки не пустые
            if (!isLeftDirectionAvailable || !isRightDirectionAvailable
                    && !isTopDirectionAvailable || !isBottomDirectionAvailable) {
                return;
            } //если
            // недоступно одно направление слева или справа, то мы не сможем ничего вписать по горизонтали
            //аналогично по вертикали
        }

        //запускаем функции, которые вписывают буквы в ячейки
        //УСЛОВИЕ 1. Если подстрока substr1 пустая
        if (substr1.equals("") && !substr2.equals("")) {
            //  то вписываем substr2 вправо
            if (isRightDirectionAvailable && isRightCellAvailable) {
                writeRight();
                if (!flagRight) {//успешно вписано справа
                    horCount++;
                    usedList.add(wordsCursor.getInt(0));
                    //если прошло успешно, записываем в horCount и в список использованных
                    return;//выходим из метода
            } else if (!isRightDirectionAvailable || !isRightCellAvailable || flagRight){
                    //затем если получили flagRight  == true, или в правом направлении вписать
                    // невозможно, то вписываем в нижнем направлении
                    if (isBottomDirectionAvailable && isBottomCellAvailable) {
                        writeBottom();
                        if (!flagBottom) //успешно вписано снизу
                            verCount++;
                        usedList.add(wordsCursor.getInt(0));
                        //если прошло успешно, записываем в verCount и в список использованных
                        { return; }//выходим из метода
                        //если направление вниз недоступно
                    } else { return; }//выходим из метода
                }
            }


        //УСЛОВИЕ 2. Если подстрока substr2 пустая
        } else if (!substr1.equals("") && substr2.equals("")) { //если пустая substr2
            //  то вписываем substr1 влево
            if (isLeftDirectionAvailable && isLeftCellAvailable) {
                writeLeft();
                if (!flagLeft) {//успешно вписано справа
                    horCount++;
                    usedList.add(wordsCursor.getInt(0));
                    //если прошло успешно, записываем в horCount и в список использованных
                    return;//выходим из метода
                } else if (!isLeftDirectionAvailable || !isLeftCellAvailable || flagLeft){
                    //затем если получили flagLeft  == true, или в левом направлении вписать
                    // невозможно, то вписываем в верхнем направлении
                    if (isTopDirectionAvailable && isTopCellAvailable) {
                        writeTop();
                        if (!flagTop) //успешно вписано снизу
                            verCount++;
                        usedList.add(wordsCursor.getInt(0));
                        //если прошло успешно, записываем в verCount и в список использованных
                        { return; }//выходим из метода
                        //если направление вниз недоступно
                    } else { return; }//выходим из метода
                }

            }


        //УСЛОВИЕ 3. Если обе подстроки не пустые
        } else {
            //вписываем горизонтально
            if (isLeftDirectionAvailable && isLeftCellAvailable
                    && isRightDirectionAvailable && isRightCellAvailable) {
                writeRight();
                if (!flagRight) { //если успешно разместили справа, пытаемся разместить слева
                    writeLeft();
                    if (!flagLeft) { //если успешно разместили слева,
                        horCount++;
                        usedList.add(wordsCursor.getInt(0));
                        //если прошло успешно, записываем в horCount и в список использованных
                        return;//выходим из метода
                    } else { //если получили флаг слева, то очищаем ячейки, заполненные справа
                        for (int m = 0; m < currentWordCellsRight.size(); m++) { //стираем вписанные справа буквы
                            currentWordCellsRight.get(m).getCellId().setText(""); //вставляем пустую строку в ячейку
                            currentWordCellsRight.get(m).setLetter(""); //и в объект
                            currentWordCellsRight.clear(); //очищаем список заполненных на этом этапе ячеек
                        } //если не получилось вписать по горизонтали, вписываем по вертикали
                    }
                }
            } else if (isTopDirectionAvailable && isTopCellAvailable //если возможно вписать вверх и вниз
                    && isBottomDirectionAvailable && isBottomCellAvailable && (flagRight || flagLeft)) {//но нельзя вправо и влево
                 // то пытаемся вписать вверх
                writeTop();
                if (flagTop) { return; } //если получили флаг справа, выходим из метода, т.к. нельзя
                // вписать ни гориз., ни вертикально
                else {
                    writeBottom(); //иначе вписываем снизу
                    if (flagBottom) { //елси получили флаг снизу, стираем то, что вписано сверху
                        for (int m = 0; m < currentWordCellsTop.size(); m++) { //стираем вписанные сверху буквы
                            currentWordCellsTop.get(m).getCellId().setText(""); //вставляем пустую строку в ячейку
                            currentWordCellsTop.get(m).setLetter(""); //и в объект
                            currentWordCellsTop.clear(); //очищаем список заполненных на этом этапе ячеек
                        }
                        return; //выходим из метода
                    } else { //если флаг снизу фолс, то размещение по вертикали прошло успешно
                        verCount++;
                        usedList.add(wordsCursor.getInt(0));
                        //если прошло успешно, записываем в verCount и в список использованных
                    }
                }
            }
        }
    }

    public boolean writeTop() {
        for (int i = appropriateCell.getHor() - 1, k = substr1.length()-1; k < 1; i--, k--) {
            letter = Character.toString(substr1.charAt(k)); //получаем букву
            for (Cell item: array) {
                if (item.getHor() == i && item.getVer() == appropriateCell.getVer()) {
                    if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                        //если ячейка пустая, вставляем букву
                        item.getCellId().setText(letter); // в ячейку
                        item.setLetter(letter);// и в объект
                        currentWordCellsTop.add(item); //записываем в список только новые буквы
                    } else if (item.getLetter().equals(letter)) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        // то выходим из цикла и переходим к вставке новой буквы
                        break;
                    } else {  //если вставлена другая буква, то стираем записанные буквы
                        for (int m = 0; m < currentWordCellsTop.size(); m++) {
                            currentWordCellsTop.get(m).getCellId().setText(""); //вставляем пустую строку в ячейку
                            currentWordCellsTop.get(m).setLetter(""); //и в объект
                            currentWordCellsTop.clear(); //очищаем список заполненных на этом этапе ячеек
                            //ставим флаг на выход из внешнего цикла
                            flagTop = true;
                        }
                        break; // и выходим из внутреннего цикла
                    }
                }
            }
            if (flagTop) {
                break;
            } //выходим из внешнего цикла
        }
        return flagTop;
    }

    public boolean writeLeft() {
        for (int i = appropriateCell.getVer() - 1, k = substr1.length()-1; k < 1; i--, k--) {
            letter = Character.toString(substr1.charAt(k)); //получаем букву
            for (Cell item: array) {
                if (item.getHor() == appropriateCell.getHor() && item.getVer() == i) {
                    if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                        //если ячейка пустая, вставляем букву
                        item.getCellId().setText(letter); // в ячейку
                        item.setLetter(letter);// и в объект
                        currentWordCellsLeft.add(item); //записываем в список только новые буквы
                    } else if (item.getLetter().equals(letter)) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        // то выходим из цикла и переходим к вставке новой буквы
                        break;
                    } else {  //если вставлена другая буква, то стираем записанные буквы
                        for (int m = 0; m < currentWordCellsLeft.size(); m++) {
                            currentWordCellsLeft.get(m).getCellId().setText(""); //вставляем пустую строку в ячейку
                            currentWordCellsLeft.get(m).setLetter(""); //и в объект
                            currentWordCellsLeft.clear(); //очищаем список заполненных на этом этапе ячеек
                            //ставим флаг на выход из внешнего цикла
                            flagLeft = true;
                        }
                        break; // и выходим из внутреннего цикла
                    }
                }
            }
            if (flagLeft) {
                break;
            } //выходим из внешнего цикла
        }
        return flagLeft;
    }

    public boolean writeRight() {
        for (int i = appropriateCell.getVer() + 1, k = 0; k < substr2.length(); i++, k++) {
            letter = Character.toString(substr2.charAt(k)); //получаем букву
            for (Cell item: array) { //находим объект cell с координатами appropriateCell.getHor
                // и i по вертикали
                if (item.getHor() == appropriateCell.getHor() && item.getVer() == i) {
                    if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                        //если ячейка пустая, вставляем букву
                        item.getCellId().setText(letter); // в ячейку
                        item.setLetter(letter);// и в объект
                        currentWordCellsRight.add(item); //записываем в список только новые буквы
                    } else if (item.getLetter().equals(letter)) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        // то выходим из цикла и переходим к вставке новой буквы
                        break;
                    } else {  //если вставлена другая буква, то стираем записанные буквы
                        for (int m = 0; m < currentWordCellsRight.size(); m++) {
                            currentWordCellsRight.get(m).getCellId().setText(""); //вставляем пустую строку в ячейку
                            currentWordCellsRight.get(m).setLetter(""); //и в объект
                            currentWordCellsRight.clear(); //очищаем список заполненных на этом этапе ячеек
                            //ставим флаг на выход из внешнего цикла
                            flagRight = true;
                        }
                        break; // и выходим из внутреннего цикла
                    }
                }
            }
            if (flagRight) {
                break;
            } //выходим из внешнего цикла
        }
        return flagRight;
    }

    public boolean writeBottom() {
        for (int i = appropriateCell.getHor() + 1, k = 0; k < substr2.length(); i++, k++) {
            letter = Character.toString(substr2.charAt(k)); //получаем букву
            for (Cell item: array) { //находим объект cell с координатами appropriateCell.getVer
                // и i по горизонтали
                if (item.getHor() == i && item.getVer() == appropriateCell.getVer()) {
                    if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                        //если ячейка пустая, вставляем букву
                        item.getCellId().setText(letter); // в ячейку
                        item.setLetter(letter);// и в объект
                        currentWordCellsBottom.add(item); //записываем в список только новые буквы
                    } else if (item.getLetter().equals(letter)) {
                        //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                        // то выходим из цикла и переходим к вставке новой буквы
                        break;
                    } else {  //если вставлена другая буква, то стираем записанные буквы
                        for (int m = 0; m < currentWordCellsBottom.size(); m++) {
                            currentWordCellsBottom.get(m).getCellId().setText(""); //вставляем пустую строку в ячейку
                            currentWordCellsBottom.get(m).setLetter(""); //и в объект
                            currentWordCellsBottom.clear(); //очищаем список заполненных на этом этапе ячеек
                            //ставим флаг на выход из внешнего цикла
                            flagBottom = true;
                        }
                        break; // и выходим из внутреннего цикла
                    }
                }
            }
            if (flagBottom) {
                break;
            } //выходим из внешнего цикла
        }
    return flagBottom;
    }


    public void placeWordWithoutAppropriate() {
        //цикл, который прописывает букву в таблицу и сохраняет ее в параметры объекта
        if (direction == 1) { //для гориз.ориентации
            for (int i = ver, k = 0; k < currentWord.length(); i++, k++) {
                letter = Character.toString(currentWord.charAt(k)); //получаем букву
                for (Cell item: array) { //находим объект cell с координатами i и ver
                    if (item.getHor() == hor && item.getVer() == i) {
                        if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                            //если ячейка пустая, вставляем букву
                            item.getCellId().setText(letter);
                            item.setLetter(letter);
                            CurrentWordCells.add(item.getCellId()); //записываем в цикл только новые буквы
                            break; //выходим из цикла и переходим к следующей ячейке
                        } else if (item.getLetter().equals(letter)) {
                            //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                            // то переходим к следующей ячейке на пути - выходим из цикла
                                break;
                        } else {  //если вставлена другая буква, то стираем записанные буквы
                            for (int m = 0; m < CurrentWordCells.size(); m++) {
                                CurrentWordCells.get(m).setText("");
                            }
                            return; // и выходим из метода
                        }
                    }
                }
            }
        }

        if (direction == 2) {//для верт.ориентации
            for (int j = hor, k = 0; k < currentWord.length(); j++, k++) {
                letter = Character.toString(currentWord.charAt(k)); //получаем букву
                for (Cell item: array) { //находим объект cell с координатами hor и j
                    if (item.getHor() == j && item.getVer() == ver) {
                        if (item.getLetter().equals("")) {// проверка на незанятость ячейки
                            //если ячейка пустая, вставляем букву
                            item.getCellId().setText(letter);
                            item.setLetter(letter);
                            CurrentWordCells.add(item.getCellId()); //записываем в цикл только новые буквы
                            break; //выходим из цикла и переходим к следующей ячейке
                        } else if (item.getLetter().equals(letter)) {
                            //проверяем, совпадают ли буквы уже вставленная и буква нового слова
                            // то переходим к следующей ячейке на пути - выходим из цикла
                            break;
                        } else {  //если вставлена другая буква, то стираем записанные буквы
                            for (int m = 0; m < CurrentWordCells.size(); m++) {
                                CurrentWordCells.get(m).setText("");
                            }
                            return; // и выходим из метода
                        }
                    }
                }
            }
        }

        //если слово записано успешно, то есть выхода из метода по команде return не было, то
        // в конце записываем в коллекцию использованных
        usedList.add(wordsCursor.getInt(0));
        // и в количество горизонтально или вертикально расположенных слов
        if (direction == 1) { horCount++; }
        else if (direction == 2)  { verCount++; }
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
        /*добавить вырезание 1 слова из предложения, добавить проверку, что не содержит цифр и
        др. символов*/
        currentWord = currentWord.toUpperCase(); //во избежание путаницы используем всегда заглавные буквы
    }

    public void getRandomPosition () {
        // генерируем координаты для первой буквы
        if (direction == 1) { //если ориент. гориз.
            hor = r.nextInt(CELLS_AMOUNT) + 1; // по горизонтали любое значение
            ver = r.nextInt(CELLS_AMOUNT - currentWord.length()) + 1; // генерируем
            // значение в таком диапазоне, чтобы слово точно поместилось, например, для слова из
            // пяти букв это будет от 1 до 6

        } else { // если слово располагаем по вертикали, тогда наоборот
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
                    item.getCellId().setText(letter);
                    item.setLetter(letter);
                    break;
                }
            }
        }

        //в конце записываем в коллекцию использованных
        usedList.add(wordsCursor.getInt(0));
        // и в количество горизонтально расположенных слов
        horCount++;
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