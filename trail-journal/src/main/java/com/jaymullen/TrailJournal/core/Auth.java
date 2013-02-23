package com.jaymullen.TrailJournal.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/21/13
 * Time: 7:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class Auth {
    private static final String LOG_TAG = Auth.class.getSimpleName();

    private static Auth _instance;
    private Context mContext;
    private SharedPreferences mPrefs;

    private String mCfid;
    private String mCftoken;
    private String mJournalId;

    public static final String BASE_URL = "http://www.trailjournals.com/";
    public static final String CFID = "CFID";
    public static final String CFTOKEN = "CFTOKEN";
    public static final String JOURNAL_ID = "JOURNAL_ID";

    public static Auth getInstance(Context context){

        if(_instance == null){
            _instance = new Auth(context);
        }
        return _instance;
    }

    private Auth(Context context) {
        // Log.d(TAG, "Auth service constructor being called");
        mContext = context.getApplicationContext();

        // get the shared preferences
        mPrefs = mContext.getSharedPreferences("tj.auth",
                Context.MODE_PRIVATE);

        // initialize all the values from any old session
       // mUserName = mPrefs.getString(USERNAME, null);

        initialize();
    }

    public String getJournalId() {
        return mJournalId;
    }

    public void setJournalId(String id) {
        Log.d("Auth", "response journalId: " + id);
        if(id != null){
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putString(JOURNAL_ID, id);
            edit.commit();

            mJournalId = id;
        }
    }

    public boolean isLoggedIn() {
        if (TextUtils.isEmpty(mCfid) || TextUtils.isEmpty(mCftoken)) {
            return false;
        }
        return true;
    }

    public void logout() {
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.remove(CFID);
        edit.remove(CFTOKEN);
        edit.remove(JOURNAL_ID);
        mCfid = null;
        mCftoken = null;
        mJournalId = null;
        edit.commit();
    }

    public String getTokenStringForUrl(){
        return mCfid + "&" + mCftoken;
    }
    public String getCfid(){
        return mCfid;
    }

    public String getCftoken(){
        return mCftoken;
    }

    public void setCfid(String value){
        if(value != null){
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putString(CFID, value);
            edit.commit();
            mCfid = value;
        }
    }

    public void setCftoken(String value){
        if(value != null){
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putString(CFTOKEN, value);
            edit.commit();
            mCftoken = value;
        }
    }

    private void initialize() {
        mCfid = mPrefs.getString(CFID, null);
        mCftoken = mPrefs.getString(CFTOKEN, null);
        mJournalId = mPrefs.getString(JOURNAL_ID, null);
    }
}
