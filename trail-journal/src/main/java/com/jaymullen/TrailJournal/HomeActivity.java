package com.jaymullen.TrailJournal;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
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

        getSupportActionBar().setTitle("Entries");

        mEntryList = (ListView)findViewById(R.id.list_entries);

        mAuth = mAuth.getInstance(this);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        mAdapter = new EntryCursorAdapter(this, null);

        mEntryList.setAdapter(mAdapter);

        mEntryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editEntry(position);
            }
        });

        registerForContextMenu(mEntryList);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                JournalEntry.CONTENT_URI,
                Entries.PROJECTION,
                null,
                null,
                JournalEntry.DEFAULT_SORT
        );
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
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.entry_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit_post:
                editEntry(info.position);
                return true;
            case R.id.delete_post:
                deleteEntry(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void editEntry(int position){
        Cursor c = (Cursor)mAdapter.getItem(position);
        Intent entryEditIntent = new Intent(HomeActivity.this, EntryActivity.class);
        entryEditIntent.setData(
                JournalEntry.buildJournalUri(String.valueOf(c.getInt(Entries.ID)))
        );

        entryEditIntent.setAction(Intent.ACTION_EDIT);
        startActivity(entryEditIntent);
    }

    private void deleteEntry(int position){
        Cursor c = (Cursor)mAdapter.getItem(position);
        Uri entryToDelete = JournalEntry.buildJournalUri(c.getString(Entries.ID));
        getContentResolver().delete(
                entryToDelete,
                null,
                null);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    public class EntryCursorAdapter extends CursorAdapter {
        public EntryCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView)view.findViewById(R.id.entry_title);
            title.setText(cursor.getString(Entries.TITLE));

            TextView id = (TextView)view.findViewById(R.id.entry_date);
            id.setText(cursor.getString(Entries.DATE));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.list_item_entry, parent, false);
            bindView(v, context, cursor);
            return v;
        }
    }

    public interface Entries{
        int TOKEN = 0x8;

        String[] PROJECTION = {
                JournalEntry._ID,
                JournalEntry.DATE,
                JournalEntry.TIMESTAMP,
                JournalEntry.TITLE,
                JournalEntry.IS_PUBLISHED,
                JournalEntry.TYPE,
                JournalEntry.DISPLAY_IN_JOURNAL,
                JournalEntry.END_DEST,
                JournalEntry.START_DEST,
                JournalEntry.SLEEP_LOCATION,
                JournalEntry.ENTRY_TEXT,
                JournalEntry.MILES
        };

        int ID = 0;
        int DATE = 1;
        int TIMESTAMP = 2;
        int TITLE = 3;
        int IS_PUBLISHED = 4;
        int TYPE = 5;
        int DISPLAY = 6;
        int END = 7;
        int START = 8;
        int SLEEP_LOC = 9;
        int ENTRY_TEXT = 10;
        int MILES = 11;

    }
}