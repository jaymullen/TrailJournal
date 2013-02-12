package com.jaymullen.TrailJournal;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockActivity;
import com.evernote.client.oauth.android.EvernoteSession;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;

import java.util.List;

public class EvernoteActivity extends SherlockActivity {
    private static final String LOG_TAG = EvernoteActivity.class.getSimpleName();

    // Your Evernote API key. See http://dev.evernote.com/documentation/cloud/
    // Please obfuscate your code to help keep these values secret.
    private static final String CONSUMER_KEY = "jamomu2-4817";
    private static final String CONSUMER_SECRET = "c1597328c865c803";

    private static final String EVERNOTE_HOST = EvernoteSession.HOST_SANDBOX;

    private final int DIALOG_PROGRESS = 101;

    private EvernoteSession mEvernoteSession;

    private Button mBtnAuth;
    private ListView mNoteList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mBtnAuth = (Button)findViewById(R.id.btn_login);
        mNoteList = (ListView) findViewById(R.id.list_notes);

        setupSession();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUi();
    }

    // using createDialog, could use Fragments instead
    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                return new ProgressDialog(EvernoteActivity.this);
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_PROGRESS:
                ((ProgressDialog) dialog).setIndeterminate(true);
                dialog.setCancelable(false);
                ((ProgressDialog) dialog).setMessage(getString(R.string.label_loading));
        }
    }
    /**
     * Setup the EvernoteSession used to access the Evernote API.
     */
    private void setupSession() {

        // Retrieve persisted authentication information
        mEvernoteSession = EvernoteSession.init(this, CONSUMER_KEY, CONSUMER_SECRET, EVERNOTE_HOST, null);
    }

    /**
     * Called when the user taps the "Log in to Evernote" button.
     * Initiates the Evernote OAuth process, or logs out if the user is already
     * logged in.
     */
    public void startAuth(View view) {
        if (mEvernoteSession.isLoggedIn()) {
            mEvernoteSession.logOut(getApplicationContext());
        } else {
            mEvernoteSession.authenticate(this);
        }
        updateUi();
    }

    /**
     * Update the UI based on Evernote authentication state.
     */
    private void updateUi() {
        if (mEvernoteSession.isLoggedIn()) {
            mBtnAuth.setText(R.string.label_logout);
            new EvernoteNotebookFetcher().execute();
        } else {
            mBtnAuth.setText(R.string.label_login);
        }
    }
    /**
     * Called when the control returns from an activity that we launched.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult: " + resultCode + " data: " + data);
        switch (requestCode) {
            //Update UI when oauth activity returns result
            case EvernoteSession.REQUEST_CODE_OAUTH:
                if (resultCode == Activity.RESULT_OK) {
                    updateUi();
                }
                break;
        }
    }


    private class EvernoteNotebookFetcher extends AsyncTask<Void, Void, List<Notebook>> {
        // using showDialog, could use Fragments instead
        @SuppressWarnings("deprecation")
        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_PROGRESS);
        }

        @Override
        protected List<Notebook> doInBackground(Void... arg){
            List<Notebook> notebooks;
            try{
                //mEvernoteSession.createNoteStore().
                notebooks = mEvernoteSession.createNoteStore().listNotebooks(mEvernoteSession.getAuthToken());
            } catch (Exception e){
                notebooks = null;
            }
            return notebooks;
        }

        // using removeDialog, could use Fragments instead
        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(List<Notebook> notebooks) {
            removeDialog(DIALOG_PROGRESS);

            if (notebooks == null) {
                Toast.makeText(getApplicationContext(), R.string.error_fetching_notes, Toast.LENGTH_LONG).show();
                return;
            }
            mNoteList.setAdapter(new NoteBookAdapter(EvernoteActivity.this, R.layout.list_item_notebook, notebooks));

        }
    }

    private class EvernoteNoteFetcher extends AsyncTask<String, Void, NoteList> {
        // using showDialog, could use Fragments instead
        @SuppressWarnings("deprecation")
        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_PROGRESS);
        }

        @Override
        protected NoteList doInBackground(String... arg){
            String notebookGuid = arg[0];

            NoteList notes;
            try{
                NoteFilter filter = new NoteFilter();
                filter.setNotebookGuid(notebookGuid);
                notes = mEvernoteSession.createNoteStore().findNotes(mEvernoteSession.getAuthToken(), filter, 0, 100);
            } catch (Exception e){
                notes = null;
            }
            return notes;
        }

        // using removeDialog, could use Fragments instead
        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(NoteList notes) {
            removeDialog(DIALOG_PROGRESS);

            if (notes == null) {
                Toast.makeText(getApplicationContext(), R.string.error_fetching_notes, Toast.LENGTH_LONG).show();
                return;
            }
            mNoteList.setAdapter(new NoteAdapter(EvernoteActivity.this, R.layout.list_item_notebook, notes.getNotes()));

        }
    }

    private class NoteBookAdapter extends ArrayAdapter<Notebook>
    {
        int mResourceId;

        private NoteBookAdapter(Context context, int resourceId, List<Notebook> objects) {
            super(context, resourceId, objects);
            mResourceId = resourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final Notebook nb = getItem(position);
            if(convertView == null){
                LayoutInflater inflater = (EvernoteActivity.this).getLayoutInflater();
                convertView = inflater.inflate(mResourceId, parent, false);
            }
            TextView tv = (TextView)convertView.findViewById(R.id.notebook_title);
            tv.setText(nb.getName());

            convertView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    new EvernoteNoteFetcher().execute(nb.getGuid());
                }
            });
            return convertView;
        }


    }

    private class NoteAdapter extends ArrayAdapter<Note>
    {
        int mResourceId;

        private NoteAdapter(Context context, int resourceId, List<Note> objects) {
            super(context, resourceId, objects);
            mResourceId = resourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final Note note = getItem(position);
            if(convertView == null){
                LayoutInflater inflater = (EvernoteActivity.this).getLayoutInflater();
                convertView = inflater.inflate(mResourceId, parent, false);
            }
            TextView tv = (TextView)convertView.findViewById(R.id.notebook_title);
            tv.setText(note.getTitle());

            return convertView;
        }


    }
}

