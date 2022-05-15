package com.example.sqliteapp.ui.list;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.sqliteapp.ui.list.DatabaseHelper;
import com.example.sqliteapp.EditActivity;
import com.example.sqliteapp.MainActivity;
import com.example.sqliteapp.R;
import com.example.sqliteapp.StudyActivity;
import com.example.sqliteapp.WordsAdapter;


public class ListFragment extends Fragment {

    ListView wordList;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    Button addBtn, studyBtn;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated (View view,  Bundle savedInstanceState) {

        databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.create_db();

        studyBtn = view.findViewById(R.id.studyButton);
        addBtn = view.findViewById(R.id.addButton);
        wordList = view.findViewById(R.id.list);
        wordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), EditActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        studyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StudyActivity.class);
                startActivity(intent);
            }
        });

        db = databaseHelper.open();
        wordsCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
       /* String[] headers = new String[]{DatabaseHelper.COLUMN_TARGET, DatabaseHelper.COLUMN_NATIVE,
                DatabaseHelper.COLUMN_STUDY};*/
        WordsAdapter wordsAdapter = new WordsAdapter(getActivity(), wordsCursor);
        wordList.setAdapter(wordsAdapter);
    }

/*    @Override
    public void onResume() {
        super.onResume();
        db = databaseHelper.open();
        wordsCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
       /* String[] headers = new String[]{DatabaseHelper.COLUMN_TARGET, DatabaseHelper.COLUMN_NATIVE,
                DatabaseHelper.COLUMN_STUDY};*/
 //       WordsAdapter wordsAdapter = new WordsAdapter(getActivity(), wordsCursor);
 //       wordList.setAdapter(wordsAdapter);
 //   }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
        wordsCursor.close();
    }

}