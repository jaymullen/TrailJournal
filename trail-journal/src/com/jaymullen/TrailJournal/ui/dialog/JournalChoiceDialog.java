package com.jaymullen.TrailJournal.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.jaymullen.TrailJournal.R;
import com.jaymullen.TrailJournal.core.JournalItem;
import com.jaymullen.TrailJournal.core.SingleTypeAdapter;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 3/2/13
 * Time: 2:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class JournalChoiceDialog extends SingleChoiceDialogFragment {

    private static final String TAG = "JournalChoiceDialog";

    private static class JournalListAdapter extends SingleTypeAdapter<JournalItem> {

        private final int selected;

        public JournalListAdapter(LayoutInflater inflater, JournalItem[] journals,
                               int selected) {
            super(inflater, android.R.layout.simple_list_item_1);

            this.selected = selected;
            setItems(journals);
        }

        @Override
        public long getItemId(int position) {
            return Long.valueOf(getItem(position).id);
        }

        @Override
        protected int[] getChildViewIds() {
            return new int[] { android.R.id.text1 };
        }

        @Override
        protected void update(int position, JournalItem item) {
            setText(0, item.name);
            //setChecked(2, selected == position);
        }
    }

    /**
     * Get selected user from results bundle
     *
     * @param arguments
     * @return user
     */
    public static JournalItem getSelected(Bundle arguments) {
        return (JournalItem) arguments.getSerializable(ARG_SELECTED);
    }

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     * @param choices
     * @param selectedChoice
     */
    public static void show(final DialogFragmentActivity activity,
                            final int requestCode, final String title, final String message,
                            ArrayList<JournalItem> choices, final int selectedChoice) {
        show(activity, requestCode, title, message, choices, selectedChoice,
                new JournalChoiceDialog());
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();

        final AlertDialog dialog = createDialog();
//        dialog.setButton(BUTTON_NEGATIVE, activity.getString(string.cancel),
//                this);
//        dialog.setButton(BUTTON_NEUTRAL, activity.getString(string.clear), this);
        dialog.setCancelable(false);

        LayoutInflater inflater = activity.getLayoutInflater();

        ListView view = (ListView) inflater.inflate(R.layout.dialog_journal_choices,
                null);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                onClick(dialog, position);
            }
        });

        ArrayList<JournalItem> choices = getChoices();
        int selected = arguments.getInt(ARG_SELECTED_CHOICE);
        JournalListAdapter adapter = new JournalListAdapter(inflater,
                choices.toArray(new JournalItem[choices.size()]), selected);
        view.setAdapter(adapter);
        if (selected >= 0)
            view.setSelection(selected);
        dialog.setView(view);

        return dialog;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<JournalItem> getChoices() {
        return (ArrayList<JournalItem>) getArguments().getSerializable(ARG_CHOICES);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        switch (which) {
            case BUTTON_NEGATIVE:
                break;
            case BUTTON_NEUTRAL:
                onResult(RESULT_OK);
                break;
            default:
                getArguments().putSerializable(ARG_SELECTED,
                        getChoices().get(which));
                onResult(RESULT_OK);
        }
    }
}
