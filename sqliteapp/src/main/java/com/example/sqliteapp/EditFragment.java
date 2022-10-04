/* Фрагмент с фунцкионалом редактирования слов и удаления из базы*/

package com.example.sqliteapp;

import android.content.ContentValues;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import com.example.sqliteapp.databinding.FragmentEditBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.apache.commons.lang3.StringUtils;

public class EditFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private FragmentEditBinding binding;
    String targetLangWord;
    int checkedDigit;
    DatabaseHelper sqlHelper;
    SQLiteDatabase db;
    Cursor editCursor;
    long wordId;
    private NavController navController;

    public EditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        binding = FragmentEditBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        binding.toggleBtn.setOnCheckedChangeListener(this);
        this.navController = Navigation.findNavController(view);

        sqlHelper = new DatabaseHelper(getActivity());
        db = sqlHelper.open();
        if (getArguments() != null) {
            wordId = getArguments().getLong("id");
        }
        // если wordId = 0, то производим добавление нового слова, оно происходит при нажатии на
        // кнопку сохранить. Если же wordId > 0, то выполняем редактирование/удаление слова с этим id
        if (wordId > 0) {
            // получаем элемент по id из бд
            editCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE + " where " +
                    DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(wordId)});
            editCursor.moveToFirst();
            targetLangWord = editCursor.getString(1);
            binding.targetBox.setText(targetLangWord);
            binding.nativeBox.setText(editCursor.getString(2));
            //получаем и выставляем булево значение учить/не учить
            if (editCursor.getInt(3) == 1) {
                binding.toggleBtn.setChecked(true);
                checkedDigit = 1;
            } else if (editCursor.getInt(3) == 0) {
                binding.toggleBtn.setChecked(false);
                checkedDigit = 0;
            } else {
                //если будет null или еще какая-то ошибка, то устанавливаем значение true и
                //выводим toast
                binding.toggleBtn.setChecked(true);
                checkedDigit = 1;
                Toast.makeText(getActivity(),
                        "Пожалуйста, укажите, нужно ли включать это слово в обучение",
                        Toast.LENGTH_LONG).show();
            }
            editCursor.close();
        } else {
            // скрываем кнопку удаления
            binding.deleteButton.setVisibility(View.GONE);
        }

        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String targetWord = binding.targetBox.getText().toString();
                String nativeWord = binding.nativeBox.getText().toString();

                if (!StringUtils.isBlank(targetWord) && !StringUtils.isBlank(nativeWord)) {
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COLUMN_TARGET, targetWord);
                    cv.put(DatabaseHelper.COLUMN_NATIVE, nativeWord);
                    cv.put(DatabaseHelper.COLUMN_STUDY, checkedDigit);

                    if (wordId > 0) {
                        db.update(DatabaseHelper.TABLE, cv,
                                DatabaseHelper.COLUMN_ID + "=" + wordId, null);
                    } else {
                        db.insert(DatabaseHelper.TABLE, null, cv);
                    }
                    goHome();

                } else {
                    Toast.makeText(getActivity(), "Пожалуйста, заполните обе строки",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goHome() {
        // закрываем подключение к БД
        db.close();
        navController.navigate(R.id.action_global_listFragment);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked)
            checkedDigit = 1;
        else
            checkedDigit = 0;
    }

    private void showBottomSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);
        Button btnYes = bottomSheetDialog.findViewById(R.id.btnYes);
        Button btnNo = bottomSheetDialog.findViewById(R.id.btnNo);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.delete(DatabaseHelper.TABLE, "_id = ?",
                        new String[]{String.valueOf(wordId)});
                bottomSheetDialog.dismiss();
                goHome();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}