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
import com.jaymullen.TrailJournal.wizard.ui.LocationFragment;

import java.util.ArrayList;

/**
 * A page asking for a name and an email.
 */
public class LocationPage extends Page {
    public static final String DESTINATION_DATA_KEY = "destination";
    public static final String START_DATA_KEY = "start";

    public LocationPage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return LocationFragment.create(getKey());
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Your name", mData.getString(DESTINATION_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Your email", mData.getString(START_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(DESTINATION_DATA_KEY));
    }
}
