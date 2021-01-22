package com.example.follower.screens.logs

import android.content.Context
import android.net.Uri
import com.example.follower.helper.rx.BaseComposers
import io.reactivex.Single
import java.io.IOException
import javax.inject.Inject

class FileInteractor @Inject constructor(private val context: Context, private val baseComposers: BaseComposers) {
    fun copyFile(originalFileUri: Uri, targetFileUri: Uri): Single<FileCreationResult> = Single.just(originalFileUri to targetFileUri)
        .doOnSuccess {
            val input = context.contentResolver.openInputStream(it.first)
            val output = context.contentResolver.openOutputStream(it.second)
            if (output != null) {
                val bytesCopied = input?.copyTo(output)
                require((bytesCopied ?: 0L) > 0L)
            } else {
                throw IOException() /*todo handle empty file impossibility to send*/
            }
        }
        .map<FileCreationResult> { FileCreationResult.Success(targetFileUri) }
        .onErrorReturn { FileCreationResult.IOError }
        .compose(baseComposers.applySingleSchedulers())
}