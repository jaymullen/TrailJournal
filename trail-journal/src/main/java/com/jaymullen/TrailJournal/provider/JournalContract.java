package com.jaymullen.TrailJournal.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/22/13
 * Time: 11:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class JournalContract {

    interface JournalColumns {
        String JOURNAL_ID = "journalId";
        String NAME = "journalName";
    }

    interface JournalEntryColumns {
        String TYPE = "type";
        String DATE = "date";
        String TIMESTAMP = "timestamp";
        String TITLE = "title";
        String START_DEST = "startDest";
        String END_DEST = "endDest";
        String SLEEP_LOCATION = "sleepLocation";
        String MILES = "miles";
        String DISPLAY_IN_JOURNAL = "displayInJournal";
        String ENTRY_TEXT = "entry_text";
        String IS_PUBLISHED = "isPublished";
        String JOURNAL_ID = "journalID";
    }

    interface LocationColumns {
        String NAME = "name";
    }

    public static final String CONTENT_AUTHORITY = "com.jaymullen.trailjournal";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
            + CONTENT_AUTHORITY);

    private static final String PATH_JOURNAL_ENTRIES = "journal_entry";
    private static final String PATH_LOCATION = "location";
    private static final String PATH_JOURNAL = "journal";

    public static class Journal implements JournalColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_JOURNAL).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.trail_journal.journal";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.trail_journal.journal";

        public static String getKey(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = JournalColumns.JOURNAL_ID + " DESC";

        /** Build {@link Uri} for requested {@link #_ID}. */
        public static Uri buildJournalUri(String key) {
            return CONTENT_URI.buildUpon().appendPath(key).build();
        }
    }

    public static class JournalEntry implements JournalEntryColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_JOURNAL_ENTRIES).build();

        /** Status constants **/
        public static final int NOT_PUBLISHED = 0;
        public static final int PUBLISHED = 1;

        public static final String TYPE_PREP = "Prep";
        public static final String TYPE_TRAIL = "Trail";

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.trail_journal.entry";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.trail_journal.entry";

        public static String getKey(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = JournalEntryColumns.TIMESTAMP + " DESC";

        /** Build {@link Uri} for requested {@link #_ID}. */
        public static Uri buildJournalUri(String key) {
            return CONTENT_URI.buildUpon().appendPath(key).build();
        }
    }

    public static class Location implements LocationColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.trail_journal.location";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.trail_journal.location";

        public static String getKey(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = LocationColumns.NAME + " DESC";

        /** Build {@link Uri} for requested {@link #_ID}. */
        public static Uri buildLocationUri(String key) {
            return CONTENT_URI.buildUpon().appendPath(key).build();
        }
    }

    private JournalContract() {
    }
}
