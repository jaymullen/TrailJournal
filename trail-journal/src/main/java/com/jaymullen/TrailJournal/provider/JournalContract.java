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

    interface JournalEntryColumns {
        String TYPE = "type";
        String DATE = "date";
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

    public static final String CONTENT_AUTHORITY = "com.jaymullen.trailjournal";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
            + CONTENT_AUTHORITY);

    private static final String PATH_JOURNAL_ENTRIES = "journal_entry";

    public static class JournalEntry implements JournalEntryColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_JOURNAL_ENTRIES).build();

        /** Status's for favorites **/
        public static final int NOT_PUBLISHED = 0;
        public static final int PUBLISHED = 1;

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.trail_journal.entry";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.trail_journal.entry";

        public static String getKey(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = JournalEntryColumns.DATE + " ASC";

        /** Build {@link Uri} for requested {@link #_ID}. */
        public static Uri buildFavoriteUri(String key) {
            return CONTENT_URI.buildUpon().appendPath(key).build();
        }
    }
    private JournalContract() {
    }
}