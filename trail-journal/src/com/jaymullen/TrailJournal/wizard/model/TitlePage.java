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

package com.jaymullen.TrailJournal.wizard.model;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.jaymullen.TrailJournal.provider.JournalContract;
import com.jaymullen.TrailJournal.wizard.ui.TitleFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A page asking for a name and an email.
 */
public class TitlePage extends Page {
    /* This may be a strange name but it corresponds to form fields on the site.  DO NOT CHANGE */
    public static final String TITLE_DATA_KEY = JournalContract.JournalEntry.TITLE;

    private String mTitle;

    public TitlePage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
        mTitle = title;
    }

    public void setValue(String title){
        mTitle = title;
        mData.putString(TITLE_DATA_KEY, title);

    }
    @Override
    public Fragment createFragment() {
        return TitleFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem(mTitle, mData.getString(TITLE_DATA_KEY), getKey(), -1));
    }

    @Override
    public void getReviewItemsForForm(HashMap<String, String> dest) {
        dest.put(TITLE_DATA_KEY, mData.getString(TITLE_DATA_KEY));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(TITLE_DATA_KEY));
    }
}
