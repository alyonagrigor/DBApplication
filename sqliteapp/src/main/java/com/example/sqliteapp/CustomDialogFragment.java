package com.example.sqliteapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.annotation.NonNull;

public class CustomDialogFragment extends androidx.fragment.app.DialogFragment {
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

            String word = getArguments().getString("word");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            return builder
                    .setMessage("Вы действительно хотите удалить слово \"" + word + "\"?")
                    .setTitle("Вы уверены?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK", null)
                    .setNegativeButton("Отмена", null)
                    .create();
    }
}

