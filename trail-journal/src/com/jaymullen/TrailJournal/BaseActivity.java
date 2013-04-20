package com.jaymullen.TrailJournal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.jaymullen.TrailJournal.core.Auth;
import com.jaymullen.TrailJournal.core.Utils;
import com.jaymullen.TrailJournal.ui.dialog.DialogFragmentActivity;
import com.jaymullen.TrailJournal.ui.dialog.DialogResultListener;
import com.jaymullen.TrailJournal.ui.dialog.ProgressDialogTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/21/13
 * Time: 5:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseActivity extends DialogFragmentActivity implements DialogResultListener {

    private static final int LOGIN_REQUEST = 0x99;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();

        if(!Auth.getInstance(this).isLoggedIn()){
            inflater.inflate(R.menu.login, menu);
        } else {
            inflater.inflate(R.menu.logout, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.login){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }
        else if(item.getItemId() == R.id.logout){
            new LogoutTask(this).execute();
        }
        else if(item.getItemId() == R.id.add_post){
            Intent addIntent = new Intent(this, EntryActivity.class);
            startActivity(addIntent);
        }
        return false;
    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        super.onDialogResult(requestCode, resultCode, arguments);

        if(requestCode == LOGIN_REQUEST && resultCode == RESULT_OK){

        }
    }

    public class LogoutTask extends ProgressDialogTask<Void, Void, Boolean> {
        public LogoutTask(Context context) {
            super(context, "Logging out...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpParams httpParams = new BasicHttpParams();
            HttpClientParams.setRedirecting(httpParams, false);

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient(httpParams);

            Auth auth = Auth.getInstance(mContext);

            StringBuilder url = new StringBuilder("http://www.trailjournals.com/login/logout.cfm?id=");
            url.append(auth.getJournalId());
            url.append("&").append(auth.getCfid());
            url.append("&").append(auth.getCftoken());

            try {

                HttpGet logout = Utils.getGet(url.toString());

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(logout);

                auth.logout();

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            invalidateOptionsMenu();
        }

    }
}
