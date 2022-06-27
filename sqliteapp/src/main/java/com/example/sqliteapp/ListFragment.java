package com.example.sqliteapp;

import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_OPEN;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListFragment extends Fragment {

    ListView wordList;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor wordsCursor;
    NavController navController;
    TextView textViewList;
    Button listButton;
    Context context;
    private Controllable controllable;

    public ListFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            controllable = (Controllable) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс Controllable");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        this.navController = Navigation.findNavController(view);
        textViewList = view.findViewById(R.id.textViewList);
        listButton = view.findViewById(R.id.listButton);
        wordList = view.findViewById(R.id.list);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE &&
                metrics.widthPixels > 600 && metrics.heightPixels > 600) {
            Toast.makeText(getActivity(), metrics.widthPixels + "x" + metrics.heightPixels, Toast.LENGTH_LONG).show();
            controllable.setDrawer_Locked();
        }

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
            wordList.setVisibility(View.GONE);
            textViewList.setVisibility(View.VISIBLE);
            listButton.setVisibility(View.VISIBLE);
            listButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navController.navigate(R.id.addFragment);
                }
            });
        }

        WordsAdapter wordsAdapter = new WordsAdapter(getActivity(), wordsCursor);
        wordList.setAdapter(wordsAdapter);

        wordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        controllable.setDrawer_Unlocked();
        if (db != null) {
            db.close();
        }
    }
}

