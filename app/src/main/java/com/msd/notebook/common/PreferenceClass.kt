package com.msd.notebook.common

import android.content.Context
import android.content.SharedPreferences

class PreferenceClass(context: Context?) {
    private val mPref: SharedPreferences
    private var mEditor: SharedPreferences.Editor? = null

    init {
        mPref = context!!.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)
    }

    fun putString(key: String?, `val`: String?) {
        try {
            doEdit()
            mEditor!!.putString(key, `val`)
            doCommit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getString(key: String?): String? {
        return mPref.getString(key, "")
    }

    private fun doEdit() {
        mEditor = mPref.edit()
    }

    private fun doCommit() {
        if (mEditor != null) {
            mEditor!!.commit()
            mEditor = null
        }
    }

    companion object {
        // TODO: CHANGE THIS TO SOMETHING MEANINGFUL
        private const val SETTINGS_NAME = "MyPref"
        private var sSharedPrefs: PreferenceClass? = null
        fun getInstance(context: Context): PreferenceClass? {
            if (sSharedPrefs == null) {
                sSharedPrefs = PreferenceClass(context.applicationContext)
            }
            return sSharedPrefs
        }
    }
}
