package com.qa.automation.android.hook;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;


public class FragmentActivityAdapter {

    private FragmentActivity myFragmentActivity;

    public FragmentActivityAdapter(Activity activity){
        myFragmentActivity=(FragmentActivity) activity;
    }

    public FragmentActivity getFragmentActivity(){
        return myFragmentActivity;
    }
}
