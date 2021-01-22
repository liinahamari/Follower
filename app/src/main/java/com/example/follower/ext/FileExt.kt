package com.example.follower.ext

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.follower.model.Directory
import java.io.File

const val DEFAULT_DEBUG_LOG_FILE_NAME = "debug.log"

fun Context.getFileSize(uri: Uri): Long = contentResolver.query(uri, null, null, null, null)?.use {
    it.moveToFirst()
    return@use it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
} ?: 0L

fun Context.getFileName(uri: Uri): String = contentResolver.query(uri, null, null, null, null)?.use {
    it.moveToFirst()
    return@use it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
}!!

fun newFile(root: File, directory: String): File = File(root, directory).apply {
    if (exists().not()) {
        mkdir()
    }
}