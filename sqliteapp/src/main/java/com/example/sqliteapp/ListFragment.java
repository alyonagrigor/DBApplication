package com.example.sqliteapp;

import android.content.Context;
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

import com.example.sqliteapp.databinding.FragmentListBinding;

public class ListFragment extends Fragment {
    FragmentListBinding binding;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    NavController navController;

    public ListFragment() {
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        this.navController = Navigation.findNavController(view);

        databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.create_db();
    }

    @Override
    public void onResume() {
        super.onResume();
        db = databaseHelper.open();
        wordsCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);

        //если в таблице БД нет записей, то показываем текствью и кнопку с предложением добавить слово
        if (wordsCursor.getCount() == 0) {
            binding.list.setVisibility(View.GONE);
            binding.textViewList.setVisibility(View.VISIBLE);
            binding.listButton.setVisibility(View.VISIBLE);
            binding.listButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navController.navigate(R.id.addFragment);
                }
            });
        }

        WordsAdapter wordsAdapter = new WordsAdapter(getActivity(), wordsCursor);
        binding.list.setAdapter(wordsAdapter);

        binding.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundleToEdit = new Bundle();
                bundleToEdit.putLong("id", id);
                navController.navigate(R.id.action_listFragment_to_editFragment, bundleToEdit);
            }
        });
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

