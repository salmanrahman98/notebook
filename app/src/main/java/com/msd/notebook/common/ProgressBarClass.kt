package com.msd.notebook.common

import android.app.ProgressDialog
import android.content.Context
import com.msd.notebook.R

class ProgressBarClass {
    var context: Context? = null
    private var progressDialog: ProgressDialog? = null

    fun showProgress(context: Context) {
        try {
            if (progressDialog != null && progressDialog!!.isShowing) {
                return
            }
            progressDialog = ProgressDialog(context)
            progressDialog!!.setMessage(context.getString(R.string.please_wait))
            progressDialog!!.progress = context.resources.getColor(R.color.indigo_blue)
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showProgress(context: Context, message: String) {
        try {
            if (progressDialog != null && progressDialog!!.isShowing) {
                return
            }
            progressDialog = ProgressDialog(context)
            progressDialog!!.setMessage(message)
            progressDialog!!.progress = context.resources.getColor(R.color.indigo_blue)
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissProgress() {
        try {
            if (null != progressDialog && progressDialog!!.isShowing) progressDialog!!.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val instance = ProgressBarClass()
    }
}