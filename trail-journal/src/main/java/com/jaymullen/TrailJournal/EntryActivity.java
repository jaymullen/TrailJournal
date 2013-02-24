/*
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jaymullen.TrailJournal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.jaymullen.TrailJournal.core.Auth;
import com.jaymullen.TrailJournal.core.Utils;
import com.jaymullen.TrailJournal.wizard.EntryWizardModel;
import com.jaymullen.TrailJournal.wizard.model.*;
import com.jaymullen.TrailJournal.wizard.ui.PageFragmentCallbacks;
import com.jaymullen.TrailJournal.wizard.ui.ReviewFragment;
import com.jaymullen.TrailJournal.wizard.ui.StepPagerStrip;
import com.jaymullen.TrailJournal.provider.JournalContract.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntryActivity extends SherlockFragmentActivity implements
        PageFragmentCallbacks,
        ReviewFragment.Callbacks,
        ModelCallbacks {

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private boolean mEditingAfterReview;

    private AbstractWizardModel mWizardModel = new EntryWizardModel(this);

    private boolean mConsumePageSelectedEvent;

    private Button mNextButton;
    private Button mPrevButton;
    private Button mSaveButton;

    private Uri mEntryUri;

    private List<Page> mCurrentPageSequence;
    private StepPagerStrip mStepPagerStrip;

    private DialogInterface.OnClickListener mPublishListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            publishEntry();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        String title;
        if(intent.getData() != null){
            mEntryUri = intent.getData();
            Cursor c = getContentResolver().query(
                    mEntryUri,
                    HomeActivity.Entries.PROJECTION,
                    null,
                    null,
                    JournalEntry.DEFAULT_SORT);

            if(c.moveToFirst()){
                title = c.getString(HomeActivity.Entries.DATE);

                setPostType(c.getString(HomeActivity.Entries.TYPE));

                setValuesOnPages(c);
            } else {
                title = "New Entry";
            }
            c.close();


        } else {
            title = "New Entry";
            ContentValues cv = new ContentValues();
            cv.put(JournalEntry.JOURNAL_ID, Auth.getInstance(this).getJournalId());
            cv.put(JournalEntry.IS_PUBLISHED, 0);
            mEntryUri = getContentResolver().insert(JournalEntry.CONTENT_URI, cv);
        }

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState != null) {
            mWizardModel.load(savedInstanceState.getBundle("model"));
        }

        mWizardModel.registerListener(this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
            @Override
            public void onPageStripSelected(int position) {
                position = Math.min(mPagerAdapter.getCount() - 1, position);
                if (mPager.getCurrentItem() != position) {
                    mPager.setCurrentItem(position);
                }
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);
        mSaveButton = (Button) findViewById(R.id.save_button);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateBottomBar();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPager.getCurrentItem() == mCurrentPageSequence.size()) {
                    DialogFragment dg = new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            return new AlertDialog.Builder(getActivity())
                                    .setMessage(R.string.submit_confirm_message)
                                    .setPositiveButton(R.string.submit_confirm_button, mPublishListener)
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .create();
                        }
                    };
                    dg.show(getSupportFragmentManager(), "place_order_dialog");
                } else {
                    if (mEditingAfterReview) {
                        mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
                    } else {
                        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    }
                }
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: implement
            }
        });

        onPageTreeChanged();

        if(intent.getAction() == Intent.ACTION_EDIT){
            mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
        }

        updateBottomBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.save_draft, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                // This is called when the Home (Up) button is pressed
                // in the Action Bar.
                Intent parentActivityIntent = new Intent(this, HomeActivity.class);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();
                return true;
            }
            case R.id.save_draft: {
                saveEntry();
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mWizardModel.getCurrentPageSequence().size() <= 1){
            getContentResolver().delete(
                    mEntryUri,
                    null,
                    null);
        } else {
            saveEntry();
        }
    }

    @Override
    public void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        recalculateCutOffPage();
        mStepPagerStrip.setPageCount(mCurrentPageSequence.size() + 1); // + 1 = review step
        mPagerAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    private void saveEntry(){
        HashMap<String, String> reviewItems = new HashMap<String, String>();
        for (Page page : mWizardModel.getCurrentPageSequence()) {
            page.getReviewItemsForForm(reviewItems);
        }

        ContentValues cv = new ContentValues();
        for(Map.Entry<String, String> entry : reviewItems.entrySet()){
            //convert date to timestamp
            if(entry.getKey().equals(JournalEntry.DATE)){
                cv.put(JournalEntry.TIMESTAMP, getTimestamp(entry.getValue()));
            }
            Log.d("Values", "key: " + entry.getKey() + " value: " + entry.getValue());
            cv.put(entry.getKey(), entry.getValue());
        }

        Log.d("Review", "uri: " + mEntryUri);
        getContentResolver().update(mEntryUri, cv, null, null);
    }

    private long getTimestamp(String dateString){

        try{
            DateFormat formatter ;
            Date date ;
            formatter = new SimpleDateFormat("MM/dd/yyyy");
            date = (Date)formatter.parse(dateString);

            return date.getTime();
        } catch (ParseException e){
            return 0;
        }
    }
    private boolean publishEntry(){


        return false;
    }
    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == mCurrentPageSequence.size()) {
                mNextButton.setText(R.string.publish);
                mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
            if(Utils.isOnline(this)){
                mNextButton.setBackgroundResource(R.drawable.finish_background);
                mNextButton.setClickable(true);
            } else {
                mNextButton.setBackgroundResource(R.drawable.finish_no_connection_background);
                mNextButton.setClickable(false);
            }

            mSaveButton.setVisibility(View.VISIBLE);
        } else {
            mNextButton.setText(mEditingAfterReview
                    ? R.string.review
                    : R.string.next);
            mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
            mNextButton.setTextAppearance(this, v.resourceId);
            mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());

            mSaveButton.setVisibility(View.GONE);
        }

        mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWizardModel.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", mWizardModel.save());
    }

    @Override
    public AbstractWizardModel onGetModel() {
        return mWizardModel;
    }

    @Override
    public void onEditScreenAfterReview(String key) {
        for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                mPager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    @Override
    public void onPageDataChanged(Page page) {
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + 1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i >= mCurrentPageSequence.size()) {
                return new ReviewFragment();
            }

            return mCurrentPageSequence.get(i).createFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO: be smarter about this
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {
            return Math.min(mCutOffPage + 1, mCurrentPageSequence.size() + 1);
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }

    private void setPostType(String type){
        for(Page p : mWizardModel.getCurrentPageSequence()){
            if(p instanceof BranchPage){
                ((BranchPage)p).setValue(type);
                ((BranchPage)p).notifyDataChanged();
            }
        }
    }

    private void setValuesOnPages(Cursor c){
        Log.d("Values", "pages size: " + mWizardModel.getCurrentPageSequence().size());
        for(Page p : mWizardModel.getCurrentPageSequence()){
            if(p instanceof TitlePage){
                ((TitlePage)p).setValue(c.getString(HomeActivity.Entries.TITLE));
            }
            else if(p instanceof MilesPage){
                ((MilesPage)p).setValue(c.getString(HomeActivity.Entries.MILES));
            }
            else if(p instanceof BodyPage){
                ((BodyPage)p).setValue(c.getString(HomeActivity.Entries.ENTRY_TEXT));
            }
            else if(p instanceof DatePage){
                ((DatePage)p).setValue(c.getString(HomeActivity.Entries.DATE));
            }
            else if(p instanceof LocationPage){
                ((LocationPage)p).setStartValue(c.getString(HomeActivity.Entries.START));
                ((LocationPage)p).setEndValue(c.getString(HomeActivity.Entries.END));
            }
        }
    }
}
