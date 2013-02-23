package com.jaymullen.TrailJournal;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import com.jaymullen.TrailJournal.core.Auth;
import com.jaymullen.TrailJournal.core.SubmitPrepEntryTask;
import com.jaymullen.TrailJournal.core.SubmitTrailEntryTask;
import com.jaymullen.TrailJournal.ui.dialog.LoginDialogFragment;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/21/13
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class HomeActivity extends BaseActivity {
    private WebView mWebView;
    private Button mPrep;
    private Auth mAuth;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mPrep = (Button)findViewById(R.id.submit_prep);

        mPrep.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new SubmitTrailEntryTask(HomeActivity.this, "submitting prep entry").execute();
            }
        });
        //mWebView = (WebView)findViewById(R.id.webview);
        mAuth = mAuth.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if(mAuth.isLoggedIn()){
//            String url = "http://www.trailjournals.com/login/welcome.cfm?" + mAuth.getCfid() + "&" + mAuth.getCftoken();
//            mWebView.loadUrl(url);
//        }
    }
}