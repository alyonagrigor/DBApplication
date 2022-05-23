/** не работает диалоговое окно, крашится при открытии
 * добавить (нвыерное) в графе в xml
 *         <action
 *             android:id="@+id/action_editFragment_to_customDialogFragment"
 *             app:destination="@id/customDialogFragment" />
 *
 *                 <dialog
 *         android:id="@+id/customDialogFragment"
 *         android:name="com.example.sqliteapp.CustomDialogFragment" >
 *         <argument
 *             android:name="word"
 *             app:argType="string" />
 *         <argument
 *             android:name="wordId"
 *             app:argType="long" />
 *         <action
 *             android:id="@+id/myaction"
 *             app:destination="@+id/studyActivity"/>
 *     </dialog>
 */

package com.example.sqliteapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

public class EditFragment extends Fragment implements CompoundButton.OnCheckedChangeListener
       //  , Removable
{
    EditText targetBox,nativeBox;
    Button delButton, saveButton;
    String targetLangWord;
    SwitchCompat toggleBtn;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit, container, false);
    }

    @Override
    public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {

        targetBox = view.findViewById(R.id.targetBox);
        nativeBox = view.findViewById(R.id.nativeBox);
        delButton = view.findViewById(R.id.deleteButton);
        saveButton = view.findViewById(R.id.saveButton);
        toggleBtn = view.findViewById(R.id.toggleBtn);
        toggleBtn.setOnCheckedChangeListener(this);
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
            targetBox.setText(targetLangWord);
            nativeBox.setText(editCursor.getString(2));
            //получаем и выставляем булево значение учить/не учить
            if (editCursor.getInt(3) == 1) {
                toggleBtn.setChecked(true);
                checkedDigit = 1;
            } else if (editCursor.getInt(3) == 0) {
                toggleBtn.setChecked(false);
                checkedDigit = 0;
            } else {
                //если будет null или еще какая-то ошибка, то устанавливаем значение true и
                //выводим toast
                toggleBtn.setChecked(true);
                checkedDigit = 1;
                Toast.makeText(getActivity(),
                        "Пожалуйста, укажите, нужно ли включать это слово в обучение",
                        Toast.LENGTH_LONG).show();
            }
            editCursor.close();
        } else {
            // скрываем кнопку удаления
            delButton.setVisibility(View.GONE);
        }

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.delete(DatabaseHelper.TABLE, "_id = ?", new String[]{String.valueOf(wordId)});
                goHome();
            /*    CustomDialogFragment dialog = new CustomDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("word", targetLangWord);
                bundle.putLong("wordId", wordId);
                dialog.setArguments(bundle);
                navController.navigate(R.id.action_editFragment_to_customDialogFragment, bundle);*/
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String targetWord = targetBox.getText().toString();
                String nativeWord = nativeBox.getText().toString();

                if (!StringUtils.isBlank(targetWord) && !StringUtils.isBlank(nativeWord)) {
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COLUMN_TARGET, targetWord);
                    cv.put(DatabaseHelper.COLUMN_NATIVE, nativeWord);
                    cv.put(DatabaseHelper.COLUMN_STUDY, checkedDigit);

                    if (wordId > 0) {
                        db.update(DatabaseHelper.TABLE, cv, DatabaseHelper.COLUMN_ID + "=" + wordId, null);
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


   /*     @Override
    public void remove(long deleteWordId2) {
        db.delete(DatabaseHelper.TABLE, "_id = ?", new String[]{String.valueOf(deleteWordId2)});
        goHome();
    }
    }*/

    private void goHome() {
        // закрываем подключение
        db.close();
        navController.navigate(R.id.action_global_listFragment);
        // переход к списку слов ??? добавить переход обратно в учить??? если пришел из учить
    /*    Intent intent = new Intent(getActivity(), ListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);*/
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked)
            checkedDigit = 1;
        else
            checkedDigit = 0;
    }
}