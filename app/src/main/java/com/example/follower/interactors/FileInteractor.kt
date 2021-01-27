package com.example.follower.interactors

import android.content.Context
import android.net.Uri
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.screens.logs.FileCreationResult
import io.reactivex.Single
import java.io.IOException
import javax.inject.Inject
/*todo handle empty file impossibility to send*/
class FileInteractor @Inject constructor(private val context: Context, private val baseComposers: BaseComposers) {
    fun copyFile(originalFileUri: Uri, targetFileUri: Uri): Single<FileCreationResult> = Single.just(originalFileUri to targetFileUri)
        .doOnSuccess {
            val input = context.contentResolver.openInputStream(it.first)
            val output = context.contentResolver.openOutputStream(it.second)
                val bytesCopied = input!!.copyTo(output!!)
                require((bytesCopied) > 0L)
        }
        .map<FileCreationResult> { FileCreationResult.Success(targetFileUri) }
        .onErrorReturn { FileCreationResult.IOError }
        .compose(baseComposers.applySingleSchedulers())
}