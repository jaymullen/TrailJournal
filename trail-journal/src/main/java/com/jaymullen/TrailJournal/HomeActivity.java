package com.jaymullen.TrailJournal;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.jaymullen.TrailJournal.core.Auth;
import com.jaymullen.TrailJournal.provider.JournalContract.JournalEntry;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/21/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class HomeActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0x02;
    private ListView mEntryList;
    private Auth mAuth;
    private CursorAdapter mAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mEntryList = (ListView)findViewById(R.id.list_entries);

        mAuth = mAuth.getInstance(this);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        mAdapter = new EntryCursorAdapter(this, null);

        mEntryList.setAdapter(mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, JournalEntry.CONTENT_URI, Entries.PROJECTION, null, null, JournalEntry.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if(mAuth.isLoggedIn()){
//            String url = "http://www.trailjournals.com/login/welcome.cfm?" + mAuth.getCfid() + "&" + mAuth.getCftoken();
//            mWebView.loadUrl(url);
//        }
    }

    public class EntryCursorAdapter extends CursorAdapter {
        public EntryCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView)view.findViewById(R.id.entry_title);
            title.setText(cursor.getString(Entries.TITLE));

            TextView id = (TextView)view.findViewById(R.id.entry_id);
            id.setText(cursor.getString(Entries.ID));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.list_item_entry, parent, false);
            bindView(v, context, cursor);
            return v;
        }
    }

    interface Entries{
        int TOKEN = 0x8;

        String[] PROJECTION = {
                JournalEntry._ID,
                JournalEntry.DATE,
                JournalEntry.TITLE,
                JournalEntry.IS_PUBLISHED,
                JournalEntry.TYPE,
                JournalEntry.DISPLAY_IN_JOURNAL,
                JournalEntry.END_DEST,
                JournalEntry.START_DEST,
                JournalEntry.SLEEP_LOCATION,
                JournalEntry.MILES
        };

        int ID = 0;
        int DATE = 1;
        int TITLE = 2;
        int IS_PUBLISHED = 3;
        int TYPE = 4;
        int DISPLAY = 5;
        int END = 6;
        int START = 7;
        int SLEEP_LOC = 8;
        int MILES = 9;

    }
}