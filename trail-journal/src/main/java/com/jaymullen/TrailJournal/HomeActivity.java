package com.jaymullen.TrailJournal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.jaymullen.TrailJournal.core.Auth;
import com.jaymullen.TrailJournal.provider.JournalContract;
import com.jaymullen.TrailJournal.provider.JournalContract.*;
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
    private SharedPreferences mPrefs;
    private static final String ACTIVE_ADAPTER_KEY = "activeAdapter";
    private ListView mEntryList;
    private Auth mAuth;
    private CursorAdapter mAdapter;
    private String mActiveJournalId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        ActionBar ab = getSupportActionBar();

        mPrefs = getPreferences(MODE_PRIVATE);

        if(savedInstanceState != null){
            mActiveJournalId = savedInstanceState.getString(ACTIVE_ADAPTER_KEY);
        } else {
            mActiveJournalId = mPrefs.getString(ACTIVE_ADAPTER_KEY, null);
        }

        Cursor c = getContentResolver().query(Journal.CONTENT_URI, Journals.PROJECTION, null, null, Journal.DEFAULT_SORT);

        if(c.moveToFirst()){
            ab.setDisplayShowTitleEnabled(false);
            ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

            final CursorAdapter ca = new CursorAdapter(this, c, false) {
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    return getLayoutInflater().inflate(R.layout.list_item_spinner, null, false);
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    TextView tv = (TextView)view.findViewById(R.id.journal_title);
                    tv.setText(cursor.getString(Journals.NAME));
                }
            };

            ab.setListNavigationCallbacks(ca, new ActionBar.OnNavigationListener() {
                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                    Cursor activeJournal = (Cursor)ca.getItem(itemPosition);
                    if(activeJournal != null){

                        mActiveJournalId = activeJournal.getString(Journals.JOURNAL_ID);
                        mAuth.setJournalId(mActiveJournalId);
                        mPrefs.edit().putString(ACTIVE_ADAPTER_KEY, mActiveJournalId).commit();

                        Log.d("Submit", "setListNavigationCallbacks() active journal: " + mActiveJournalId + " for item at: " + itemPosition + " itemId: " + itemId);
                        getSupportLoaderManager().restartLoader(LOADER_ID, null, HomeActivity.this);
                    } else {
                        Log.d("Submit", "cursor was empty");
                    }
                    //activeJournal.close();

                    return false;
                }
            });

        } else {
            ab.setTitle("Entries");
        }


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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ACTIVE_ADAPTER_KEY, mActiveJournalId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("Submit", "onCreateLoader()");
        CursorLoader loader;
        if(mActiveJournalId != null){
            loader = new CursorLoader(
                    this,
                    JournalEntry.CONTENT_URI,
                    Entries.PROJECTION,
                    JournalEntry.JOURNAL_ID + "=?",
                    new String[]{ mActiveJournalId },
                    JournalEntry.DEFAULT_SORT
            );
        } else {
            loader = new CursorLoader(
                    this,
                    JournalEntry.CONTENT_URI,
                    Entries.PROJECTION,
                    null,
                    null,
                    JournalEntry.DEFAULT_SORT
            );
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("Submit", "onLoadFinished()");
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("Submit", "onLoaderReset()");
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
            case R.id.view_entries:
                viewEntries();
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

    private void viewEntries(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.trailjournals.com/date.cfm?trailname=" + mActiveJournalId));
        startActivity(browserIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Cursor c = getContentResolver().query(Journal.CONTENT_URI, Journals.PROJECTION, null, null, Journal.DEFAULT_SORT);

        if(c.moveToFirst()){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

            if(mActiveJournalId != null){
                while(!c.isAfterLast()){
                    if(c.getString(Journals.JOURNAL_ID).equals(mActiveJournalId)){
                        getSupportActionBar().setSelectedNavigationItem(c.getPosition());
                    }
                    c.moveToNext();
                }
            }
        }
        c.close();
    }

    public class EntryCursorAdapter extends CursorAdapter {
        public EntryCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView date = (TextView)view.findViewById(R.id.entry_date);
            TextView start = (TextView)view.findViewById(R.id.entry_start_location);
            TextView end = (TextView)view.findViewById(R.id.entry_end_location);
            TextView startLabel = (TextView)view.findViewById(R.id.entry_location_start_label);
            TextView endLabel = (TextView)view.findViewById(R.id.entry_location_end_label);
            ImageView contextArrow = (ImageView)view.findViewById(R.id.entry_options);
            ImageView publishIndicator = (ImageView) view.findViewById(R.id.publish_status);

            int publishStatus = cursor.getInt(Entries.IS_PUBLISHED);
            if(publishStatus == JournalEntry.PUBLISHED){
                publishIndicator.setEnabled(true);
            } else {
                publishIndicator.setEnabled(false);
            }

            date.setText(cursor.getString(Entries.DATE));

            String type = cursor.getString(Entries.TYPE);
            if(type != null){
                if(type.equals(JournalEntry.TYPE_PREP)){
                    start.setText(cursor.getString(Entries.TITLE));
                    end.setVisibility(View.GONE);
                    startLabel.setVisibility(View.GONE);
                    endLabel.setVisibility(View.GONE);
                }
                else if(type.equals(JournalEntry.TYPE_TRAIL)){
                    Log.d("Adapter", "Start: " + cursor.getString(Entries.START) + " end: " + cursor.getString(Entries.END));
                    start.setText(cursor.getString(Entries.START));
                    end.setText(cursor.getString(Entries.END));

                    end.setVisibility(View.VISIBLE);
                    startLabel.setVisibility(View.VISIBLE);
                    endLabel.setVisibility(View.VISIBLE);
                }
            }

            contextArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     openContextMenu(v);
                }
            });
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.list_item_entry, parent, false);
            bindView(v, context, cursor);
            return v;
        }
    }

    public interface Journals{
        int TOKEN = 0x7;

        String[] PROJECTION = {
                Journal._ID,
                Journal.NAME,
                Journal.JOURNAL_ID
        };

        int ID = 0;
        int NAME = 1;
        int JOURNAL_ID = 2;
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
                JournalEntry.MILES,
                JournalEntry.ENTRY_ID
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
        int ENTRY_ID = 12;
    }
}