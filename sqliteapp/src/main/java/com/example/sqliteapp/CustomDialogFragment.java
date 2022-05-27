package com.example.sqliteapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CustomDialogFragment extends DialogFragment {

    private Removable removable;
    String word;
    long deleteWordId;

 /*   @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        removable = (Removable) context;
    }*/

    public CustomDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog, container, false);
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null) {
            word = getArguments().getString("word");
            long deleteWordId = getArguments().getLong ("wordId");
        }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            return builder
                    .setMessage("Вы действительно хотите удалить слово \"" + word + "\"?")
                    .setTitle("Вы уверены?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        //    removable.remove(deleteWordId);
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .create();
    }
}

