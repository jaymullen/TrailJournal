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
import com.jaymullen.TrailJournal.wizard.ui.LocationFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A page asking for a starting and ending location
 */
public class LocationPage extends Page {
    public static final String DESTINATION_DATA_KEY = JournalContract.JournalEntry.END_DEST;
    public static final String START_DATA_KEY = JournalContract.JournalEntry.START_DEST;

    public LocationPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return LocationFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Destination", mData.getString(DESTINATION_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Starting Location", mData.getString(START_DATA_KEY), getKey(), -1));
    }

    @Override
    public void getReviewItemsForForm(HashMap<String, String> dest) {
        dest.put(DESTINATION_DATA_KEY, mData.getString(DESTINATION_DATA_KEY));
        dest.put(START_DATA_KEY, mData.getString(START_DATA_KEY));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(DESTINATION_DATA_KEY));
    }
}
