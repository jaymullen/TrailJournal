package com.jaymullen.TrailJournal.core;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.jaymullen.TrailJournal.provider.JournalContract;
import com.jaymullen.TrailJournal.provider.JournalContract.*;
import com.jaymullen.TrailJournal.ui.dialog.ProgressDialogTask;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/22/13
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubmitTrailEntryTask extends ProgressDialogTask<Uri, Void, Boolean> {

    private Auth mAuth;

    public SubmitTrailEntryTask(Context context, String message) {
        super(context, message);
    }

    @Override
    protected Boolean doInBackground(Uri... entryUris) {

        if(entryUris.length == 0)
            return false;

        mAuth = Auth.getInstance(mContext);

        HttpParams httpParams = new BasicHttpParams();
        HttpClientParams.setRedirecting(httpParams, false);

        // Create a new HttpClient and Post Header
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        for(Uri entryUri : entryUris){
            Cursor c = mContext.getContentResolver().query(
                    entryUri,
                    Entries.PROJECTION,
                    null,
                    null,
                    JournalEntry.DEFAULT_SORT
            );

            if(c.moveToFirst()){
                String type = c.getString(Entries.TYPE);
                if(type.equals(JournalEntry.TYPE_PREP)){
                    publishPrepEntry(httpClient, c);
                }
                else if(type.equals(JournalEntry.TYPE_TRAIL)){
                    publishTrailEntry(httpClient, c);
                }

                c.close();
            } else {
                c.close();
                continue;
            }
        }

        return null;
    }

    public boolean publishPrepEntry(HttpClient httpClient, Cursor c){
          return false;
    }

    public boolean publishTrailEntry(HttpClient httpClient, Cursor c){
        String url = Auth.BASE_URL + "login/admin_RecordAction.cfm?" + mAuth.getTokenStringForUrl();
        Log.d("Submit", "submitting trail entry to: " + url);

        try {
            String display = c.getString(Entries.DISPLAY).equals("Yes") ? "1" : "0";
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("FieldList", "date_entry,Entry,photo_id,Destination,Miles,datecreated,type,sleeploc,display,startlocation,journalid"));
            nameValuePairs.add(new BasicNameValuePair("journalID", c.getString(Entries.JOURNAL_ID)));
            nameValuePairs.add(new BasicNameValuePair("datecreated", Utils.getNowString(false)));
            nameValuePairs.add(new BasicNameValuePair("date_entry", c.getString(Entries.DATE)));
            nameValuePairs.add(new BasicNameValuePair("destination", c.getString(Entries.END_LOCATION)));
            //nameValuePairs.add(new BasicNameValuePair("location", "locationfield"));
            nameValuePairs.add(new BasicNameValuePair("startlocation", c.getString(Entries.START_LOCATION)));
            nameValuePairs.add(new BasicNameValuePair("type", c.getString(Entries.TYPE)));
            nameValuePairs.add(new BasicNameValuePair("sleeploc", c.getString(Entries.SLEEP_LOC)));
            nameValuePairs.add(new BasicNameValuePair("Miles", c.getString(Entries.MILES)));
            nameValuePairs.add(new BasicNameValuePair("photo_id", ""));
            nameValuePairs.add(new BasicNameValuePair("display", display));
            nameValuePairs.add(new BasicNameValuePair("Entry", c.getString(Entries.ENTRY_TEXT)));
            nameValuePairs.add(new BasicNameValuePair("texttype", "text"));
            nameValuePairs.add(new BasicNameValuePair("btnEdit_OK", "OK"));

            HttpPost httppost = Utils.getPost(url);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httppost);

            Log.d("Submit", "response: " + response.getStatusLine());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
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
                JournalEntry.JOURNAL_ID
        };

        int ID = 0;
        int DATE = 1;
        int TIMESTAMP = 2;
        int TITLE = 3;
        int IS_PUBLISHED = 4;
        int TYPE = 5;
        int DISPLAY = 6;
        int END_LOCATION = 7;
        int START_LOCATION = 8;
        int SLEEP_LOC = 9;
        int ENTRY_TEXT = 10;
        int MILES = 11;
        int JOURNAL_ID = 12;
    }
}
