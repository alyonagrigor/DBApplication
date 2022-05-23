package com.example.sqliteapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

public class AddFragment extends Fragment {

    EditText targetBox, nativeBox;
    Button saveButton, studyButton, listButton;
    DatabaseHelper sqlHelper;
    SQLiteDatabase db;

    public AddFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    public void onViewCreated (View view,  Bundle savedInstanceState) {
        saveButton = view.findViewById(R.id.saveButton);
        studyButton = view.findViewById(R.id.studyButton);
        listButton = view.findViewById(R.id.listButton);
        targetBox = view.findViewById(R.id.targetBox);
        nativeBox = view.findViewById(R.id.nativeBox);
        sqlHelper = new DatabaseHelper(getActivity());
        sqlHelper.create_db();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String targetWord = targetBox.getText().toString();
                String nativeWord = nativeBox.getText().toString();

                if (!StringUtils.isBlank(targetWord) && !StringUtils.isBlank(nativeWord)) {
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COLUMN_TARGET, targetWord);
                    cv.put(DatabaseHelper.COLUMN_NATIVE, nativeWord);
                    db.insert(DatabaseHelper.TABLE, null, cv);
                    Toast.makeText(getActivity(), "Успешно сохранено",
                            Toast.LENGTH_SHORT).show();
                    targetBox.setText("");
                    nativeBox.setText("");

                } else {
                    Toast.makeText(getActivity(), "Пожалуйста, заполните обе строки",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

 /*       listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ListActivity.class);
                startActivity(intent);
            }
        });

            studyButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), StudyActivity.class);
            startActivity(intent);
        }
    });*/
}

    @Override
    public void onResume() {
        super.onResume();
        db = sqlHelper.open();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }
}