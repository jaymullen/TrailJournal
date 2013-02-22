package com.jaymullen.TrailJournal;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.jaymullen.TrailJournal.core.Auth;
import com.jaymullen.TrailJournal.core.Utils;
import com.jaymullen.TrailJournal.ui.dialog.ProgressDialogTask;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_fragment);

        mUser= (EditText)findViewById(R.id.username_text);
        mPass = (EditText)findViewById(R.id.password_text);
        mRemember = (CheckBox)findViewById(R.id.remember_check);
        mLogin = (Button) findViewById(R.id.login_btn);

        mUser.setText("mullen.jm@gmail.com");
        mPass.setText("aip2Ohna");
        mLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new LoginTask(LoginActivity.this).execute(mUser.getText().toString(), mPass.getText().toString());
            }
        });
    }


    public class LoginTask extends ProgressDialogTask<String, Void, Boolean> {
        public LoginTask(Context context) {
            super(context, "Authenticating...");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String user;
            String pass;

            if(params[0] != null){
                user = params[0];
                pass = params[1];
            } else {
                return false;
            }

            HttpParams httpParams = new BasicHttpParams();
            HttpClientParams.setRedirecting(httpParams, false);

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient(httpParams);

            StringBuilder url = new StringBuilder("http://www.trailjournals.com/login/welcome.cfm?now=");
            url.append(getNowString());

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

                Log.d("LOGIN", "response: " + response);
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

                //Now we need to grab the index page so we can scrape out the journal id
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
            return true;
        }

        private String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
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
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            finish();
        }

        private String getJournalId(String html){

            String idPrefix = "href=\"logout.cfm?id=";
            int start = html.indexOf(idPrefix);
            int end   = html.indexOf("&", start);

            return html.substring(start + idPrefix.length(), end);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private String getNowString(){

            try{
                return URLEncoder.encode("{ts '" + sdf.format(new Date()) + "'}", "UTF-8");
            } catch(UnsupportedEncodingException e){

            }
            return "";
        }
    }
}