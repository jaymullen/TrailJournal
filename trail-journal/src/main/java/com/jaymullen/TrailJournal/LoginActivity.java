package com.jaymullen.TrailJournal;

import android.app.Activity;
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
import com.jaymullen.TrailJournal.core.Utils;
import com.jaymullen.TrailJournal.provider.JournalContract.*;
import com.jaymullen.TrailJournal.ui.dialog.ProgressDialogTask;
import org.apache.http.*;
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

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/21/13
 * Time: 6:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginActivity extends Activity {

    private EditText mPass;
    private EditText mUser;
    private CheckBox mRemember;
    private Button mLogin;
    private TextView mErrorMessage;

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
        mLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new LoginTask(LoginActivity.this).execute(mUser.getText().toString(), mPass.getText().toString());
            }
        });
    }

    private void displayErrorMessage(){
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    public class LoginTask extends ProgressDialogTask<String, Void, String> {

        private static final String formAction = "<form action=\"welcome.cfm?";
        private static final String trailId = "<input type=\"hidden\" name=\"id\" value=\"";
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

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("Login", user));
                nameValuePairs.add(new BasicNameValuePair("Pass", pass));
                nameValuePairs.add(new BasicNameValuePair("btn_login", " L O G I N "));
                //nameValuePairs.add(new BasicNameValuePair("sec", "on"));

                HttpPost httppost = Utils.getPost(url.toString());
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                //This is to check for auth failure... i know...
                if(response.getStatusLine().getStatusCode() == 302 && response.getFirstHeader("Location").getValue().equals("http://www.trailjournals.com/login.cfm?message=3")){
                    Log.d("Submit", "Error Will Robinson");
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
                    String html= convertStreamToString(instream);

                    if(html.contains("Choose One of Your Accounts to Edit")){
                        Log.d("Submit", "found multiple accounts");

                        int startIndex = html.indexOf(formAction);
                        int startName = html.indexOf(journalNamePattern);
                        String journalId = "";
                        if(startIndex == -1)
                            return ERROR;

                        while(startIndex != -1){
                            int idStart = html.indexOf(trailId, startIndex) + trailId.length();
                            int idEnd  = html.indexOf("\"", idStart);

                            int nameSectionStart = html.indexOf(journalNamePattern, startName) + journalNamePattern.length();
                            int nameStart = html.indexOf(journalNamePrefix, nameSectionStart) + journalNamePrefix.length();
                            int nameEnd = html.indexOf("</b></font>", nameStart);
                            //Maybe this should be trail id?
                            journalId = html.substring(idStart, idEnd);
                            String journalName = html.substring(nameStart, nameEnd);

                            Log.d("Submit", "journal id: " + journalId + " name: " + journalName);
                            ContentValues cv = new ContentValues();
                            cv.put(Journal.NAME, journalName);
                            cv.put(Journal.JOURNAL_ID, journalId);

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
                String result= convertStreamToString(instream);
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

            if(action.equals(SUCCESS) || action.equals(MULTIPLE_JOURNALS)){
                finish();
            } else if(action.equals(ERROR)) {
                displayErrorMessage();
            }
        }

        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }

        private String getJournalId(String html){

            String idPrefix = "href=\"logout.cfm?id=";
            int start = html.indexOf(idPrefix);
            int end   = html.indexOf("&", start);

            return html.substring(start + idPrefix.length(), end);
        }
    }
}