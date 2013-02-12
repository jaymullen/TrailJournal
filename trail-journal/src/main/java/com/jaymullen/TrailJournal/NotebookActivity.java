package com.jaymullen.TrailJournal;

import android.os.Bundle;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockActivity;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/12/13
 * Time: 2:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class NotebookActivity extends SherlockActivity {

    private ListView mNoteList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mNoteList = (ListView) findViewById(R.id.list_notes);
    }
}