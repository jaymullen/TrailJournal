package com.jaymullen.TrailJournal.ui.dialog;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import com.jaymullen.TrailJournal.R;

/**
 * Helper to display a confirmation dialog
 */
public class LoginDialogFragment extends DialogFragmentHelper implements
        OnClickListener {

    private static final String TAG = "login_dialog";

    private EditText mPass;
    private EditText mUser;
    private CheckBox mRemember;

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     */
    public static void show(final DialogFragmentActivity activity,
                            final int requestCode, final String title, final String message) {
        show(activity, requestCode, title, message, null);
    }

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     * @param bundle
     */
    public static void show(final DialogFragmentActivity activity,
                            final int requestCode, final String title, final String message,
                            final Bundle bundle) {
        Bundle arguments = createArguments(title, message, requestCode);
        if (bundle != null)
            arguments.putAll(bundle);
        show(activity, new LoginDialogFragment(), arguments, TAG);
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View loginView = factory.inflate(R.layout.login_fragment, null);

        mUser= (EditText)loginView.findViewById(R.id.username_text);
        mPass = (EditText)loginView.findViewById(R.id.password_text);
        mRemember = (CheckBox)loginView.findViewById(R.id.remember_check);

        AlertDialog dialog = LightAlertDialog.create(getActivity());
        dialog.setTitle(getTitle());
        //dialog.setMessage(getMessage());
        dialog.setView(loginView);
        dialog.setButton(BUTTON_POSITIVE,
                getResources().getString(R.string.login), this);
        dialog.setButton(BUTTON_NEGATIVE,
                getResources().getString(android.R.string.no), this);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(this);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        switch (which) {
            case BUTTON_POSITIVE:
                onResult(RESULT_OK);
                break;
            case BUTTON_NEGATIVE:
                onResult(RESULT_CANCELED);
                break;
        }
    }
}