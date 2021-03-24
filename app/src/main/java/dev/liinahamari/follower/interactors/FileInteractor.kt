package dev.liinahamari.follower.interactors

import android.content.Context
import android.net.Uri
import dev.liinahamari.follower.helper.rx.BaseComposers
import io.reactivex.Single
import javax.inject.Inject

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

sealed class FileCreationResult {
    data class Success(val targetFileUri: Uri): FileCreationResult()
    object IOError: FileCreationResult()
}