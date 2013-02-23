package com.jaymullen.TrailJournal.core;

import android.content.Context;
import android.util.Log;
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
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubmitPrepEntryTask extends ProgressDialogTask<String, Void, Boolean> {
    public SubmitPrepEntryTask(Context context, String message) {
        super(context, message);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Auth auth = Auth.getInstance(mContext);
        String url = Auth.BASE_URL + "login/entry_action.cfm?" + auth.getTokenStringForUrl();
                                // /login/admin_RecordAction.cfm?


        HttpParams httpParams = new BasicHttpParams();
        HttpClientParams.setRedirecting(httpParams, false);

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient(httpParams);

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("journalId", auth.getJournalId()));
            nameValuePairs.add(new BasicNameValuePair("date_entry", "02/22/2013"));
            nameValuePairs.add(new BasicNameValuePair("destination", "destination test from app"));
            nameValuePairs.add(new BasicNameValuePair("photo_id", "NULL"));
            nameValuePairs.add(new BasicNameValuePair("display", "0"));
            nameValuePairs.add(new BasicNameValuePair("Entry", "test entry strings are fun"));
            nameValuePairs.add(new BasicNameValuePair("texttype", "text"));
            nameValuePairs.add(new BasicNameValuePair("btnAdd_pre_OK", "OK"));

            HttpPost httppost = Utils.getPost(url);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            Log.d("PREP", "response: " + response.getStatusLine());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
