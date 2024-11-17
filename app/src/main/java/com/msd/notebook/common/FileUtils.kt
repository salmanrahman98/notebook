package com.msd.notebook.common

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    fun getFileFromUri(context: Context, uri: Uri): File? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)
                if (idx != -1) {
                    val filePath = it.getString(idx)
                    return File(filePath)
                }
            }
        }
        return null
    }

    @Throws(Exception::class)
    fun getFileFromUriNew(context: Context, uri: Uri?): File {
        val inputStream = context.contentResolver.openInputStream(uri!!)

        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null

        val file: File =
            makeEmptyFileIntoExternalStorageWithTitle(
                getFileName(context,uri), context
            )
        bis = BufferedInputStream(inputStream)
        bos = BufferedOutputStream(
            FileOutputStream(
                file, false
            )
        )

        val buf = ByteArray(16 * 1024)
        // bis.read(buf);
        /* do {
            bos.write(buf);
        } while (bis.read(buf) != -1);

*/
        var bytesread = 0
        while ((bis.read(buf).also { bytesread = it }) != -1) {
            bos.write(buf, 0, bytesread) //Write bytes that are read
        }
        bos.flush()
        bos.close()
        bis.close()

        return file
    }

    fun makeEmptyFileIntoExternalStorageWithTitle(title: String?, context: Context): File {
        //        String root = Environment.getExternalStorageDirectory().getAbsolutePath();

        val root: String = context.getFilesDir().getAbsolutePath()
        return File(root, title)
    }

    fun getFileName(context: Context?, uri: Uri): String? {
        var fileName: String? = ""
        if (uri.scheme == "content") {
            val cursor = context?.contentResolver?.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    fileName = it.getString(nameIndex)
                }
            }
        }
        if (fileName == null) {
            // Fallback: extract from the file path if it's not a content Uri
            fileName = uri.path?.substring(uri.path!!.lastIndexOf('/') + 1)
        }
        return fileName
    }

    //https://stackoverflow.com/questions/37951869/how-to-get-the-file-extension-in-android
    fun getfileExtension(uri: Uri?,context: Context): String? {
        val extension: String
        val contentResolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri!!)).toString()
        return extension
    }
}