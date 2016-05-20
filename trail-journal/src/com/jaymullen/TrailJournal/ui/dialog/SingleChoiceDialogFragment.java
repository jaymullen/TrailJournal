package com.jaymullen.TrailJournal.ui.dialog;

import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Helper to display a single choice dialog
 */
public class SingleChoiceDialogFragment extends DialogFragmentHelper implements
        OnClickListener {

    /**
     * Arguments key for the selected item
     */
    public static final String ARG_SELECTED = "selected";

    /**
     * Choices arguments
     */
    protected static final String ARG_CHOICES = "choices";

    /**
     * Selected choice argument
     */
    protected static final String ARG_SELECTED_CHOICE = "selectedChoice";

    /**
     * Tag
     */
    protected static final String TAG = "single_choice_dialog";

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     * @param choices
     * @param selectedChoice
     * @param helper
     */
    protected static void show(final DialogFragmentActivity activity,
                               final int requestCode, final String title, final String message,
                               ArrayList<?> choices, final int selectedChoice,
                               final DialogFragmentHelper helper) {
        Bundle arguments = createArguments(title, message, requestCode);
        arguments.putSerializable(ARG_CHOICES, choices);
        arguments.putInt(ARG_SELECTED_CHOICE, selectedChoice);
        show(activity, helper, arguments, TAG);
    }
}