package com.mvk.mweather.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;


/**
 * Created by marcin on 03/02/2017.
 */

public class AlertDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Error title")
                .setMessage("Error msg")
                .setPositiveButton("ok", null);

        AlertDialog dialogue = builder.create();
        return dialogue;
    }
}
