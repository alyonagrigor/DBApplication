package com.example.sqliteapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ListFragment extends Fragment {

    ListView wordList;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    NavController navController;
    TextView textViewList;
    Button listButton;
    Bundle bundle = new Bundle();

    public ListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {
        this.navController = Navigation.findNavController(view);
        textViewList = view.findViewById(R.id.textViewList);
        listButton = view.findViewById(R.id.listButton);;
        wordList = view.findViewById(R.id.list);
        wordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bundle.putLong("id", id);
                navController.navigate(R.id.action_listFragment_to_editFragment, bundle);
            }
        });

        databaseHelper = new DatabaseHelper(getActivity());
        // создаем базу данных
        databaseHelper.create_db();
    }

    @Override
    public void onResume() {
        super.onResume();
        // открываем подключение
        db = databaseHelper.open();
        //получаем данные из бд в виде курсора
        wordsCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
       /* String[] headers = new String[]{DatabaseHelper.COLUMN_TARGET, DatabaseHelper.COLUMN_NATIVE,
                DatabaseHelper.COLUMN_STUDY};*/
        WordsAdapter wordsAdapter = new WordsAdapter(getActivity(), wordsCursor);
        wordList.setAdapter(wordsAdapter);
        //если в таблице БД нет записей, то показываем текствью и кнопку с предложением добавить слово
        if (wordsCursor.getCount() == 0) {
            textViewList.setVisibility(View.VISIBLE);
            listButton.setVisibility(View.VISIBLE);
            listButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navController.navigate(R.id.addFragment);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение и курсор
        db.close();
        wordsCursor.close();
    }
}