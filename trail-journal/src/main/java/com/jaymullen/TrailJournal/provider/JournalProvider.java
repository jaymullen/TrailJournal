package com.jaymullen.TrailJournal.provider;

import android.database.SQLException;
import android.util.Log;
import com.jaymullen.TrailJournal.provider.JournalDatabase.*;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.jaymullen.TrailJournal.provider.JournalContract.*;
import android.content.ContentProvider;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/22/13
 * Time: 11:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class JournalProvider extends ContentProvider {
    public static final String LOG_TAG = JournalProvider.class.getSimpleName();

    private JournalDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int JOURNAL_ENTRY = 101;
    private static final int JOURNAL_ENTRY_ITEM = 102;


    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = JournalContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "journal_entry", JOURNAL_ENTRY);
        matcher.addURI(authority, "journal_entry/*", JOURNAL_ENTRY_ITEM);

        return matcher;
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case JOURNAL_ENTRY:
                return JournalEntry.CONTENT_TYPE;
            case JOURNAL_ENTRY_ITEM:
                return JournalEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new JournalDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Log.v(LOG_TAG,
        // "query(uri=" + uri + ", proj=" + Arrays.toString(projection)
        // + ")");
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        final int match = sUriMatcher.match(uri);
        final SelectionBuilder builder;
        Cursor c;
        switch (match) {
            default: {
                // Most cases are handled with simple SelectionBuilder
                builder = buildSimpleSelection(uri);
                c = builder.where(selection, selectionArgs).query(db, projection,
                        sortOrder);
                c.setNotificationUri(getContext().getContentResolver(), uri);
                return c;
            }
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Log.d(LOG_TAG, "delete(uri=" + uri + ")");
        if (uri == JournalContract.BASE_CONTENT_URI) {
            // Handle whole database deletes (e.g. when signing out)
            deleteDatabase();
            getContext().getContentResolver().notifyChange(uri, null, false);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);

        // Handle which uri's to send delete notifications too. Sometimes (eg
        // {@link SteppedListViewSettingsFragment} ) we dont' always wish to
        // recieve delete notifications only updates and inserts
        final int match = sUriMatcher.match(uri);
        switch (match) {
            default: {
                getContext().getContentResolver().notifyChange(uri, null, false);
                break;
            }
        }
        return retVal;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Log.v(LOG_TAG, "insert(uri=" + uri + ", values=" + values.toString()
        // + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        // boolean syncToNetwork =
        // !SyncContract.hasCallerIsSyncAdapterParameter(uri);
        switch (match) {
            case JOURNAL_ENTRY: {
                long id = db.insertOrThrow(Tables.JOURNAL_ENTRIES, null, values);
                getContext().getContentResolver().notifyChange(uri, null, false);
                Uri itemUri = JournalEntry.buildJournalUri(String.valueOf(id));
                return itemUri;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] allValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String table;
        switch (match) {
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

//        int rowsAdded = 0;
//        long rowId;
//
//        try {
//            db.beginTransaction();
//            for (ContentValues initialValues : allValues) {
//
//                rowId = db.insertOrThrow(table, null, initialValues);
//                if (rowId > 0)
//                    rowsAdded++;
//            }
//
//            db.setTransactionSuccessful();
//        } catch (SQLException ex) {
//            Log.e(LOG_TAG, "There was a problem with the bulk insert table: "
//                    + table);
//        } finally {
//            db.endTransaction();
//        }
//        getContext().getContentResolver().notifyChange(uri, null, false);
//        return rowsAdded;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // Log.v(LOG_TAG, "update(uri=" + uri + ", values=" + values.toString()
        // + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).update(db, values);
        getContext().getContentResolver().notifyChange(uri, null, false);

        return retVal;
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case JOURNAL_ENTRY: {
                return builder.table(Tables.JOURNAL_ENTRIES);
            }
            case JOURNAL_ENTRY_ITEM: {
                final String key = JournalEntry.getKey(uri);
                return builder.table(Tables.JOURNAL_ENTRIES).where(
                        JournalEntry._ID + "=?", key);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    private void deleteDatabase() {
        // TODO: wait for content provider operations to finish, then tear down
        mOpenHelper.close();
        Context context = getContext();
        JournalDatabase.deleteDatabase(context);
        mOpenHelper = new JournalDatabase(getContext());
    }

}
