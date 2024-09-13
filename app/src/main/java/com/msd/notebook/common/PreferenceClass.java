package com.msd.notebook.common;


import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceClass {
    // TODO: CHANGE THIS TO SOMETHING MEANINGFUL
    private static final String SETTINGS_NAME = "MyPref";
    private static PreferenceClass sSharedPrefs;
    private final SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;

    public PreferenceClass(Context context) {
        mPref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }

    public static PreferenceClass getInstance(Context context) {
        if (sSharedPrefs == null) {
            sSharedPrefs = new PreferenceClass(context.getApplicationContext());
        }
        return sSharedPrefs;
    }

    public void putString(String key, String val) {
        try {
            doEdit();
            mEditor.putString(key, val);
            doCommit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getString(String key) {
        return mPref.getString(key, "");
    }

    private void doEdit() {
        mEditor = mPref.edit();

    }

    private void doCommit() {
        if (mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }
}
