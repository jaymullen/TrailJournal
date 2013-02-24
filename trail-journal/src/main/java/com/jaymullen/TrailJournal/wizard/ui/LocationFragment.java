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

package com.jaymullen.TrailJournal.wizard.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Filterable;
import android.widget.TextView;
import com.jaymullen.TrailJournal.R;
import com.jaymullen.TrailJournal.provider.JournalContract;
import com.jaymullen.TrailJournal.wizard.model.LocationPage;

public class LocationFragment extends Fragment {
    private static final String ARG_KEY = "key";

    private PageFragmentCallbacks mCallbacks;
    private String mKey;
    private LocationPage mPage;
    private AutoCompleteTextView mStartingLocation;
    private AutoCompleteTextView mEndingLocation;

    public static LocationFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        LocationFragment fragment = new LocationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public LocationFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = (LocationPage) mCallbacks.onGetPage(mKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page_location, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        AutoCompleteAdapter adapter = new AutoCompleteAdapter(getActivity());

        mStartingLocation = ((AutoCompleteTextView) rootView.findViewById(R.id.start_location));
        mStartingLocation.setText(mPage.getData().getString(LocationPage.START_DATA_KEY));
        mStartingLocation.setAdapter(adapter);

        mEndingLocation = ((AutoCompleteTextView) rootView.findViewById(R.id.end_location));
        mEndingLocation.setText(mPage.getData().getString(LocationPage.DESTINATION_DATA_KEY));
        mEndingLocation.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStartingLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                    int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(LocationPage.START_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });

        mEndingLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
                    int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPage.getData().putString(LocationPage.DESTINATION_DATA_KEY,
                        (editable != null) ? editable.toString() : null);
                mPage.notifyDataChanged();
            }
        });
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        if (mStartingLocation != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }


    public class AutoCompleteAdapter extends CursorAdapter implements Filterable {
        //private static final String TAG = "AutoCompleteAdapter";

        private LayoutInflater mInflater;

        public AutoCompleteAdapter(Context context){
            super(context, null, 0);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View newView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return newView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tv = (TextView)view.findViewById(android.R.id.text1);
            tv.setText(cursor.getString(Locations.NAME));
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            Log.d("AutoComplete", "constraint: " + constraint);

            final String[] projection = Locations.PROJECTION;
            final StringBuilder buffer = new StringBuilder();
            buffer.append('%').append(constraint).append('%');
            final String pattern = buffer.toString();

            final StringBuilder selection = new StringBuilder();
            selection.append(JournalContract.Location.NAME);
            selection.append(" LIKE ? ");
            final String mSelection = selection.toString();

            final String[] mArguments = new String[1];
            mArguments[0] = pattern;
            return getActivity().getContentResolver().query(JournalContract.Location.CONTENT_URI, projection, mSelection, mArguments, null);
        }

        @Override
        public CharSequence convertToString(Cursor cursor) {
            return cursor.getString(Locations.NAME);
        }

        public boolean containsString(CharSequence location){
            final String[] projection = Locations.PROJECTION;
            Cursor c = getActivity().getContentResolver()
                    .query(
                            JournalContract.Location.CONTENT_URI,
                            projection,
                            JournalContract.Location.NAME + " == ?",
                            new String[]{location.toString()},
                            null);
            if(c.moveToFirst())
                return true;
            else
                return false;
        }
    }

    public interface Locations {
        int _TOKEN = 0x3;

        String[] PROJECTION = {
                BaseColumns._ID,
                JournalContract.Location.NAME
        };

        int ID = 0;
        int NAME = 1;
    }
}
