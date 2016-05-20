package com.jaymullen.TrailJournal;

import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.jaymullen.TrailJournal.core.Utils;
import com.jaymullen.TrailJournal.provider.JournalContract;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/24/13
 * Time: 1:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class JournalApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initializeData();
    }

    public void initializeData(){
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                SharedPreferences prefs = getSharedPreferences("startup", MODE_PRIVATE);
                boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
                if(isFirstRun){
                    try{
                        String shelters = Utils.loadResourceAsString(JournalApplication.this, R.raw.at_location_list);
                        String[] shelterArray = shelters.split(",");

                        final ArrayList<ContentValues> allValues = new ArrayList<ContentValues>();

                        for (String shelter : shelterArray) {

                            ContentValues cv = new ContentValues();
                            cv.put(JournalContract.Location.NAME, shelter);
                            allValues.add(cv);
                        }
                        ContentValues[] cvs = new ContentValues[allValues.size()];
                        allValues.toArray(cvs);
                        getContentResolver().bulkInsert(JournalContract.Location.CONTENT_URI, cvs);
                    } catch(IOException e){

                    }

                    prefs.edit().putBoolean("isFirstRun", false).commit();
                }
                return null;
            }
        }.execute();
    }
}
