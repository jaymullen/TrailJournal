package com.jaymullen.TrailJournal.wizard.model;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.jaymullen.TrailJournal.wizard.ui.DateFragment;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/22/13
 * Time: 4:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatePage extends Page {

    public static final String DATE_DATA_KEY = "date";

    public DatePage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return DateFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Entry Date", mData.getString(DATE_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(DATE_DATA_KEY));
    }
}
