package com.jaymullen.TrailJournal.provider;

import com.jaymullen.TrailJournal.provider.JournalContract.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/22/13
 * Time: 11:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class JournalDatabase extends SQLiteOpenHelper {
    public static final String LOG_TAG = JournalDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = "trail_journal.db";

    // NOTE: carefully update onUpgrade() when bumping database versions to make
    // sure user data is saved.
    private static final int DATABASE_VERSION = 5;

    interface Tables {
        String JOURNAL_ENTRIES = "journal_entries";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + Tables.JOURNAL_ENTRIES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + JournalEntryColumns.TYPE + " TEXT,"
                + JournalEntryColumns.DATE + " INT,"
                + JournalEntryColumns.TITLE + " TEXT,"
                + JournalEntryColumns.START_DEST + " TEXT,"
                + JournalEntryColumns.END_DEST + " TEXT,"
                + JournalEntryColumns.SLEEP_LOCATION + " TEXT,"
                + JournalEntryColumns.MILES + " INT,"
                + JournalEntryColumns.DISPLAY_IN_JOURNAL + " INT,"
                + JournalEntryColumns.ENTRY_TEXT + " TEXT,"
                + JournalEntryColumns.IS_PUBLISHED + " INT,"
                + JournalEntryColumns.JOURNAL_ID + " INT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        int version = oldVersion;

        if (version != DATABASE_VERSION) {
            Log.w(LOG_TAG, "Destroying old data during upgrade");

            db.execSQL("DROP TABLE IF EXISTS " + Tables.JOURNAL_ENTRIES);

            onCreate(db);
        }
    }

    public JournalDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
