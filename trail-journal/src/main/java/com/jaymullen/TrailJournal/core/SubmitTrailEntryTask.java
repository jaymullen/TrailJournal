package com.jaymullen.TrailJournal.core;

import android.content.ContentValues;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
        HttpClientParams.setRedirecting(httpParams, true);

        // Create a new HttpClient and Post Header
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        for(Uri entryUri : entryUris){
            boolean success = false;
            Cursor c = mContext.getContentResolver().query(
                    entryUri,
                    Entries.PROJECTION,
                    null,
                    null,
                    JournalEntry.DEFAULT_SORT
            );

            if(c.moveToFirst()){
                String type = c.getString(Entries.TYPE);
                boolean isPublished =  c.getInt(Entries.IS_PUBLISHED) == 1;
                if(type.equals(JournalEntry.TYPE_PREP) && !isPublished){
                    success = publishPrepEntry(httpClient, c, entryUri);
                }
                else if(type.equals(JournalEntry.TYPE_TRAIL)){
                    success = publishTrailEntry(httpClient, c, entryUri);
                }
            } else {
                c.close();
                continue;
            }

            c.close();
        }

        return true;
    }

    public boolean publishPrepEntry(HttpClient httpClient, Cursor c, Uri entryUri){
        Auth auth = Auth.getInstance(mContext);
        String url = Auth.BASE_URL + "login/entry_action.cfm?" + auth.getTokenStringForUrl();

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            String display = c.getString(Entries.DISPLAY).equals("Yes") ? "1" : "0";
            nameValuePairs.add(new BasicNameValuePair("journalId", c.getString(Entries.JOURNAL_ID)));
            nameValuePairs.add(new BasicNameValuePair("date_entry", c.getString(Entries.DATE)));
            nameValuePairs.add(new BasicNameValuePair("destination", c.getString(Entries.END_LOCATION)));
            nameValuePairs.add(new BasicNameValuePair("photo_id", "NULL"));
            nameValuePairs.add(new BasicNameValuePair("display", display));
            nameValuePairs.add(new BasicNameValuePair("Entry", c.getString(Entries.ENTRY_TEXT)));
            nameValuePairs.add(new BasicNameValuePair("texttype", "text"));
            nameValuePairs.add(new BasicNameValuePair("btnAdd_pre_OK", "OK"));

            HttpPost httppost = Utils.getPost(url);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httppost);

            getEntryId(response, c, entryUri);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean publishTrailEntry(HttpClient httpClient, Cursor c, Uri entryUri){
        String publishUrl = Auth.BASE_URL + "login/admin_RecordAction.cfm?" + mAuth.getTokenStringForUrl();
        Log.d("Submit", "submitting trail entry to: " + publishUrl);

        try {
            String display = c.getString(Entries.DISPLAY).equals("Yes") ? "1" : "0";

            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("FieldList", "date_entry,Entry,photo_id,Destination,Miles,datecreated,type,sleeploc,display,startlocation,journalid"));
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

            if(c.getInt(Entries.IS_PUBLISHED) == 1
                    && c.getString(Entries.ENTRY_ID) != null){
                nameValuePairs.add(new BasicNameValuePair("journalid", c.getString(Entries.JOURNAL_ID)));
                nameValuePairs.add(new BasicNameValuePair("RecordID", c.getString(Entries.ENTRY_ID)));
                nameValuePairs.add(new BasicNameValuePair("dateedit", Utils.getNowString(false)));
                nameValuePairs.add(new BasicNameValuePair("id", c.getString(Entries.ENTRY_ID)));
            } else {
                nameValuePairs.add(new BasicNameValuePair("journalID", c.getString(Entries.JOURNAL_ID)));
                nameValuePairs.add(new BasicNameValuePair("datecreated", Utils.getNowString(false)));
            }
            HttpPost httppost = Utils.getPost(publishUrl);
            httppost.setHeader("Cookie", mAuth.getCookieString());
            httppost.setHeader("Referer", "http://www.trailjournals.com/login/journal_add.cfm?" + mAuth.getTokenStringForUrl());
            httppost.setHeader("Connection", "");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httppost);

            getEntryId(response, c, entryUri);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    private void getEntryId(HttpResponse response, Cursor c, Uri entryUri) throws IOException{
        if(response.getStatusLine().getStatusCode() == 200){
            InputStream instream = response.getEntity().getContent();
            String result = Utils.convertStreamToString(instream);
            instream.close();

            HashMap<Integer, String> matches = new HashMap<Integer, String>();

            String recordHtml = "<a class=\"link\" href=\"journal_edit.cfm?recordid=";
            String dateStartHtml = "<b>";
            int startIndex = result.indexOf(recordHtml);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
            String entryId = new String();

            while(startIndex != -1){
                int start = result.indexOf(recordHtml, startIndex) + recordHtml.length();
                int end  = result.indexOf("&", start);

                int dateStart = result.indexOf(dateStartHtml, end) + dateStartHtml.length();
                int dateEnd = result.indexOf("<", dateStart);

                String id = result.substring(start, end);
                String date = result.substring(dateStart, dateEnd);

                String entryDate = sdf.format(new Date(c.getLong(Entries.TIMESTAMP)));

                Log.d("Submit", "start id: " + start + " end id " + end + " date start : " + dateStart + " date end: " + dateEnd);
                Log.d("Submit", "date from db: " + entryDate + " date from html: " + date + " entry id: " + id);

                if(date.equals(entryDate)){
                    entryId = id;
                }
                startIndex = result.indexOf(recordHtml, startIndex+recordHtml.length());
            }
            //<a class="link" href="journal_edit.cfm?recordid=
            //String entryId = getEntryIdFromDate(c.getString(Entries.DATE));
            ContentValues cv = new ContentValues();
            cv.put(JournalEntry.IS_PUBLISHED, 1);
            cv.put(JournalEntry.ENTRY_ID, entryId);
            mContext.getContentResolver().update(entryUri, cv, null, null);
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
                JournalEntry.MILES,
                JournalEntry.JOURNAL_ID,
                JournalEntry.ENTRY_ID
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
        int ENTRY_ID = 13;
    }
}
