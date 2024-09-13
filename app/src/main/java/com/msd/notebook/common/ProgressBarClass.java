package com.msd.notebook.common;

import android.app.ProgressDialog;
import android.content.Context;

import com.msd.notebook.R;

public class ProgressBarClass {
    private static final ProgressBarClass ourInstance = new ProgressBarClass();
    public Context context;
    private ProgressDialog progressDialog;

    public ProgressBarClass() {
    }
    public static ProgressBarClass getInstance() {
        return ourInstance;
    }

    public void showProgress(Context context) {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                return;
            }
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getString(R.string.please_wait));
            progressDialog.setProgress(context.getResources().getColor(R.color.indigo_blue));
            progressDialog.setCancelable(false);
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissProgress() {
        try {
            if (null != progressDialog && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}