package com.jaymullen.TrailJournal;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.jaymullen.TrailJournal.core.Auth;
import com.jaymullen.TrailJournal.core.JournalItem;
import com.jaymullen.TrailJournal.core.Utils;
import com.jaymullen.TrailJournal.provider.JournalContract.Journal;
import com.jaymullen.TrailJournal.ui.dialog.DialogFragmentActivity;
import com.jaymullen.TrailJournal.ui.dialog.JournalChoiceDialog;
import com.jaymullen.TrailJournal.ui.dialog.ProgressDialogTask;
import com.jaymullen.TrailJournal.ui.dialog.SingleChoiceDialogFragment;
import org.apache.http.Header;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/21/13
 * Time: 6:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginActivity extends DialogFragmentActivity {

    private EditText mPass;
    private EditText mUser;
    private CheckBox mRemember;
    private Button mLogin;
    private TextView mErrorMessage;

    private String mUserName;
    private String mLoginUrl;

    private ArrayList<JournalItem> journalIds = new ArrayList<JournalItem>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_fragment);

        mUser= (EditText)findViewById(R.id.username_text);
        mPass = (EditText)findViewById(R.id.password_text);
        mRemember = (CheckBox)findViewById(R.id.remember_check);
        mLogin = (Button) findViewById(R.id.login_btn);
        mErrorMessage = (TextView) findViewById(R.id.login_error_message);

        mUser.setText("mullen.jm@gmail.com");
        mPass.setText("aip2Ohna");

        mUserName = mUser.getText().toString();
        mLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new LoginTask(LoginActivity.this).execute(mUserName, mPass.getText().toString());
            }
        });
    }

    private void displayErrorMessage(){
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    public class LoginTask extends ProgressDialogTask<String, Void, String> {

        private static final String formAction = "<form action=\"welcome.cfm?";
        private static final String journalIdHtml = "<input type=\"hidden\" name=\"id\" value=\"";
        private static final String hitsHtml = "<input type=\"hidden\" name=\"hits\" value=\"";
        private static final String trailId = "<input type=\"hidden\" name=\"trailid\" value=\"";
        private static final String cId = "<input type=\"hidden\" name=\"cid\" value=\"";
        private static final String journalNamePattern = "<blockquote>Welcome <font color=\"navy\"><b></b></font><BR>";
        private static final String journalNamePrefix = "<font color=\"navy\"><b>";

        private static final String ERROR = "error";
        private static final String SUCCESS = "success";
        private static final String MULTIPLE_JOURNALS = "multiple";

        public LoginTask(Context context) {
            super(context, "Authenticating...");
        }

        @Override
        protected String doInBackground(String... params) {
            String user;
            String pass;

            if(params[0] != null){
                user = params[0];
                pass = params[1];
            } else {
                return ERROR;
            }

            HttpParams httpParams = new BasicHttpParams();
            HttpClientParams.setRedirecting(httpParams, false);

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient(httpParams);

            StringBuilder url = new StringBuilder("http://www.trailjournals.com/login/welcome.cfm?now=");
            url.append(Utils.getNowString(true));

            mLoginUrl = url.toString();
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("Login", user));
                nameValuePairs.add(new BasicNameValuePair("Pass", pass));
                nameValuePairs.add(new BasicNameValuePair("btn_login", " L O G I N "));
                //nameValuePairs.add(new BasicNameValuePair("sec", "on"));

                HttpPost httppost = Utils.getPost(mLoginUrl);
                httppost.setHeader("Referer", "http://www.trailjournals.com/login.cfm");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                //This is to check for auth failure... i know...
                if(response.getStatusLine().getStatusCode() == 302 && response.getFirstHeader("Location").getValue().equals("http://www.trailjournals.com/login.cfm?message=3")){
                    Log.d("Submit", "Error...");
                    return ERROR;
                }

                Log.d("Submit", "response: " + response);
                Auth auth = Auth.getInstance(mContext);
                for(Header header : response.getAllHeaders()){
                    if(header.getName().equals("Set-Cookie")){
                        if(header.getValue().contains(Auth.CFID)){
                            int end = header.getValue().indexOf(";");
                            auth.setCfid(header.getValue().substring(0, end));
                        }

                        if(header.getValue().contains(Auth.CFTOKEN)){
                            int end = header.getValue().indexOf(";");
                            auth.setCftoken(header.getValue().substring(0, end));
                        }
                    }
                }

                if(response.getStatusLine().getStatusCode() == 200){
                    InputStream instream = response.getEntity().getContent();
                    String html = Utils.convertStreamToString(instream);
                    instream.close();

                    if(html.contains("Choose One of Your Accounts to Edit")){
                        Log.d("Submit", "found multiple accounts");

                        int startIndex = html.indexOf(formAction);
                        int startName = html.indexOf(journalNamePattern);
                        String journalId = "";
                        if(startIndex == -1)
                            return ERROR;

                        while(startIndex != -1){
                            int idStart = html.indexOf(journalIdHtml, startIndex) + journalIdHtml.length();
                            int idEnd  = html.indexOf("\"", idStart);

                            int nameSectionStart = html.indexOf(journalNamePattern, startName) + journalNamePattern.length();
                            int nameStart = html.indexOf(journalNamePrefix, nameSectionStart) + journalNamePrefix.length();
                            int nameEnd = html.indexOf("</b></font>", nameStart);

                            int trailIdStart = html.indexOf(trailId, startIndex) + trailId.length();
                            int trailIdEnd  = html.indexOf("\"", trailIdStart);

                            int cIdStart = html.indexOf(cId, startIndex) + cId.length();
                            int cIdEnd  = html.indexOf("\"", cIdStart);

                            int hitsStart = html.indexOf(hitsHtml, startIndex) + hitsHtml.length();
                            int hitsEnd  = html.indexOf("\"", hitsStart);

                            journalId = html.substring(idStart, idEnd);
                            String journalName = html.substring(nameStart, nameEnd);
                            String trailId = html.substring(trailIdStart, trailIdEnd);
                            String cid = html.substring(cIdStart, cIdEnd);
                            String hits = html.substring(hitsStart, hitsEnd);

                            Log.d("Submit", "journal id: " + journalId + " name: " + journalName);
                            journalIds.add(new JournalItem(journalId, journalName, hits, trailId, cid));
                            ContentValues cv = new ContentValues();
                            cv.put(Journal.NAME, journalName);
                            cv.put(Journal.JOURNAL_ID, journalId);
                            cv.put(Journal.HITS, hits);
                            cv.put(Journal.TRAIL_ID, trailId);
                            cv.put(Journal.CID, cid);

                            getContentResolver().insert(Journal.CONTENT_URI, cv);

                            startName = html.indexOf(journalNamePattern, startName+journalNamePattern.length());
                            startIndex = html.indexOf(formAction, startIndex+formAction.length());
                        }

                        auth.setJournalId(journalId);
                        return MULTIPLE_JOURNALS;
                    }
                    else if(html.contains("error")){
                        return ERROR;
                    }

                }

                //We did not find multiple accounts so grab the index page to scrape the journal id
                String url2 = "http://www.trailjournals.com/login/welcome.cfm?" + auth.getCfid() + "&" + auth.getCftoken();
                HttpGet httpget = Utils.getGet(url2);

                HttpResponse response2 = httpclient.execute(httpget);
                InputStream instream = response2.getEntity().getContent();
                String result = Utils.convertStreamToString(instream);
                // now you have the string representation of the HTML request
                instream.close();

                String journalId = getJournalId(result);

                auth.setJournalId(journalId);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return SUCCESS;
        }

        @Override
        protected void onPostExecute(String action) {
            super.onPostExecute(action);

            if(action.equals(SUCCESS)){
                finish();
            }
            else if(action.equals(MULTIPLE_JOURNALS)){
                JournalChoiceDialog.show(LoginActivity.this, 3, "Select Journal To Edit", null, journalIds, 0);
            } else if(action.equals(ERROR)) {
                displayErrorMessage();
            }
        }

        private String getJournalId(String html){

            String idPrefix = "href=\"logout.cfm?id=";
            int start = html.indexOf(idPrefix);
            int end   = html.indexOf("&", start);

            return html.substring(start + idPrefix.length(), end);
        }
    }

    public class SelectJournalTask extends ProgressDialogTask<JournalItem, Void, String> {

        private static final String ERROR = "error";
        private static final String SUCCESS = "success";
        private static final String MULTIPLE_JOURNALS = "multiple";

        public SelectJournalTask(Context context) {
            super(context, "Selecting Journal...");
        }

        @Override
        protected String doInBackground(JournalItem... params) {
            JournalItem journal;

            if(params[0] != null){
                journal = params[0];
            } else {
                return ERROR;
            }

            Auth auth = Auth.getInstance(LoginActivity.this);
            HttpParams httpParams = new BasicHttpParams();
            HttpClientParams.setRedirecting(httpParams, false);

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient(httpParams);

            StringBuilder url = new StringBuilder("http://www.trailjournals.com/login/welcome.cfm?");
            url.append(auth.getTokenStringForUrl());

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("direct", "../login/welcome.cfm?trailname=" + journal.id));
                nameValuePairs.add(new BasicNameValuePair("hits", journal.hits));
                nameValuePairs.add(new BasicNameValuePair("Login", mUserName));
                nameValuePairs.add(new BasicNameValuePair("trailname", ""));
                nameValuePairs.add(new BasicNameValuePair("id", journal.id));
                nameValuePairs.add(new BasicNameValuePair("cid", journal.cid));
                nameValuePairs.add(new BasicNameValuePair("trailid", journal.trailId));
                nameValuePairs.add(new BasicNameValuePair("yamila", "0"));
                nameValuePairs.add(new BasicNameValuePair("btn_login", " E D I T  J O U R N A L "));
                //nameValuePairs.add(new BasicNameValuePair("sec", "on"));

                HttpPost httppost = Utils.getPost(url.toString());
                httppost.setHeader("Referer", mLoginUrl);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                //This is to check for auth failure... i know...
                if(response.getStatusLine().getStatusCode() != 200){
                    return ERROR;
                }

                auth.setJournalId(journal.id);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return SUCCESS;
        }

        @Override
        protected void onPostExecute(String action) {
            super.onPostExecute(action);

            if(action.equals(SUCCESS)){
                finish();
            }
            else if(action.equals(ERROR)) {
                displayErrorMessage();
            }
        }
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        JournalItem journal = (JournalItem)arguments.get(SingleChoiceDialogFragment.ARG_SELECTED);

        new SelectJournalTask(this).execute(journal);
    }
}