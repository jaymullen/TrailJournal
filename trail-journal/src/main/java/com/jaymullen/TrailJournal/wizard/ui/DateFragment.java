package com.jaymullen.TrailJournal.wizard.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.TextView;
import com.jaymullen.TrailJournal.R;
import com.jaymullen.TrailJournal.wizard.model.BodyPage;
import com.jaymullen.TrailJournal.wizard.model.DatePage;

import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: jaymullen
 * Date: 2/22/13
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class DateFragment extends Fragment {
    private static final String ARG_KEY = "key";

    private PageFragmentCallbacks mCallbacks;
    private String mKey;
    private DatePage mPage;
    private DatePicker mDateView;

    public static DateFragment create(String key) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        DateFragment fragment = new DateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public DateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = (DatePage) mCallbacks.onGetPage(mKey);
    }

    DatePicker.OnDateChangedListener mDateListener = new DatePicker.OnDateChangedListener() {
        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String date = (monthOfYear+1) + "/" + dayOfMonth + "/" + year;
            mPage.getData().putString(DatePage.DATE_DATA_KEY, date);
            mPage.notifyDataChanged();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_date, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        mDateView = ((DatePicker) rootView.findViewById(R.id.entry_date));

        String initDate;
        if(TextUtils.isEmpty(mPage.getData().getString(DatePage.DATE_DATA_KEY))){
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            mDateView.init(year, month, day, mDateListener);

            initDate = (month+1) + "/" + day + "/" + year;
            mPage.getData().putString(DatePage.DATE_DATA_KEY, initDate);
        } else {
            String[] dateParts = mPage.getData().getString(DatePage.DATE_DATA_KEY).split("/");

            if(dateParts.length == 3){
                mDateView.init(
                        Integer.valueOf(dateParts[2]),
                        Integer.valueOf(dateParts[0]) - 1,
                        Integer.valueOf(dateParts[1]),
                        mDateListener
                );
            }
        }

        mPage.notifyDataChanged();

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

    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        if (mDateView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }
}
