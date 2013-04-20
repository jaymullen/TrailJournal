package com.jaymullen.TrailJournal.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Task that runs with a progress dialog at the foreground
 *
 * @param <Params, Progress, Result>
 */
public abstract class ProgressDialogTask<Params, Progress, Result>  extends AsyncTask<Params, Progress, Result> {

    /**
     * Progress dialog last displayed
     */
    protected AlertDialog progress;
    protected String mMessage;

    protected Context mContext;
    /**
     * @param context
     */
    protected ProgressDialogTask(Context context, String message) {
        mContext = context;
        mMessage = message;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showIndeterminate(mMessage);
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        dismissProgress();
    }

    /**
     * Dismiss and clear progress dialog field
     */
    protected void dismissProgress() {
        if (progress != null) {
            progress.dismiss();
            progress = null;
        }
    }

    /**
     * Show indeterminate progress dialog with given message
     *
     * @param message
     */
    protected void showIndeterminate(final CharSequence message) {
        dismissProgress();

        progress = LightProgressDialog.create(mContext, message);
        progress.show();
    }

    /**
     * Show indeterminate progress dialog with given message
     *
     * @param resId
     */
    protected void showIndeterminate(final int resId) {
        dismissProgress();

        progress = LightProgressDialog.create(mContext, resId);
        progress.show();
    }
}