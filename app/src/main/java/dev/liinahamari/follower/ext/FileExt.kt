package dev.liinahamari.follower.ext

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.screens.logs.FILE_PROVIDER_META
import java.io.File

fun Context.createDirIfNotExist(dirName: String) = File(filesDir, dirName).apply {
    if (exists().not()) {
        mkdir()
    }
}

fun Context.createFileIfNotExist(fileName: String, dirName: String) = File(createDirIfNotExist(dirName), fileName).apply {
    if (exists().not()) {
        createNewFile()
    }
}

fun Context.getUriForInternalFile(file: File): Uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + FILE_PROVIDER_META, file)

