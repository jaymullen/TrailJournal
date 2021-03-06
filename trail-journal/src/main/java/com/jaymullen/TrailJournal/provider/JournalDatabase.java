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
        String JOURNALS = "journals";
        String JOURNAL_ENTRIES = "journal_entries";
        String LOCATIONS = "locations";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + Tables.JOURNALS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + JournalColumns.JOURNAL_ID + " TEXT,"
                + JournalColumns.NAME + " TEXT,"
                + JournalColumns.CID + " TEXT,"
                + JournalColumns.TRAIL_ID + " TEXT,"
                + JournalColumns.HITS + " TEXT,"
                + "UNIQUE (" + JournalColumns.JOURNAL_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.JOURNAL_ENTRIES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + JournalEntryColumns.ENTRY_ID + " TEXT,"
                + JournalEntryColumns.TYPE + " TEXT,"
                + JournalEntryColumns.DATE + " TEXT,"
                + JournalEntryColumns.TIMESTAMP + " INTEGER,"
                + JournalEntryColumns.TITLE + " TEXT,"
                + JournalEntryColumns.START_DEST + " TEXT,"
                + JournalEntryColumns.END_DEST + " TEXT,"
                + JournalEntryColumns.SLEEP_LOCATION + " TEXT,"
                + JournalEntryColumns.MILES + " INTEGER,"
                + JournalEntryColumns.DISPLAY_IN_JOURNAL + " TEXT,"
                + JournalEntryColumns.ENTRY_TEXT + " TEXT,"
                + JournalEntryColumns.IS_PUBLISHED + " INTEGER,"
                + JournalEntryColumns.JOURNAL_ID + " INTEGER)");

        db.execSQL("CREATE TABLE " + Tables.LOCATIONS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LocationColumns.NAME + " TEXT,"
                + "UNIQUE (" + LocationColumns.NAME + ") ON CONFLICT REPLACE)");

        //Triggers to update locations based on user input
        db.execSQL("CREATE TRIGGER " + Triggers.START_LOCATION_TRIGGER
                + " AFTER UPDATE ON " + Tables.JOURNAL_ENTRIES
                + " WHEN new." + JournalEntry.START_DEST + " IS NOT NULL"
                + " BEGIN INSERT INTO " + Tables.LOCATIONS + "(" + Location.NAME + ")"
                + " VALUES(new." + JournalEntry.START_DEST + ");"
                + " END;");

        db.execSQL("CREATE TRIGGER " + Triggers.END_LOCATION_TRIGGER
                + " AFTER UPDATE ON " + Tables.JOURNAL_ENTRIES
                + " WHEN new." + JournalEntry.END_DEST + " IS NOT NULL"
                + " BEGIN INSERT INTO " + Tables.LOCATIONS + "(" + Location.NAME + ")"
                + " VALUES(new." + JournalEntry.END_DEST + ");"
                + " END;");
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

    private interface Triggers{
        String START_LOCATION_TRIGGER = "start_location_trigger";
        String END_LOCATION_TRIGGER = "end_location_trigger";
    }
}
