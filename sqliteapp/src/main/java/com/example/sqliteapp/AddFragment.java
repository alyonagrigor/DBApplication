/* Фрагмент для добавления новых слов в БД
 */

package com.example.sqliteapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.sqliteapp.databinding.FragmentAddBinding;
import org.apache.commons.lang3.StringUtils;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    DatabaseHelper sqlHelper;
    SQLiteDatabase db;

    public AddFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        binding = FragmentAddBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    public void onViewCreated (View view,  Bundle savedInstanceState) {

        sqlHelper = new DatabaseHelper(getActivity());
        sqlHelper.create_db();

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String targetWord =  binding.targetBox.getText().toString();
                String nativeWord =  binding.nativeBox.getText().toString();

                if (!StringUtils.isBlank(targetWord) && !StringUtils.isBlank(nativeWord)) {
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COLUMN_TARGET, targetWord);
                    cv.put(DatabaseHelper.COLUMN_NATIVE, nativeWord);
                    db.insert(DatabaseHelper.TABLE, null, cv);
                    Toast.makeText(getActivity(), "Успешно сохранено",
                            Toast.LENGTH_SHORT).show();
                    binding.targetBox.setText("");
                    binding.nativeBox.setText("");

                } else {
                    Toast.makeText(getActivity(), "Пожалуйста, заполните обе строки",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        db = sqlHelper.open();
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