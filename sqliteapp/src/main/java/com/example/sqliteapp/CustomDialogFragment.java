package com.example.sqliteapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.content.Context;

public class CustomDialogFragment extends androidx.fragment.app.DialogFragment {

    private Removable removable;
    String word;
    long deleteWordId;

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        removable = (Removable) context;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null) {
            word = getArguments().getString("word");
            deleteWordId = getArguments().getLong ("wordId");
        }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            return builder
                    .setMessage("Вы действительно хотите удалить слово \"" + word + "\"?")
                    .setTitle("Вы уверены?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removable.remove(deleteWordId);
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .create();
    }
}

