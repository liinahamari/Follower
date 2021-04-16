/*
Copyright 2020-2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.follower.interactors

import android.content.Context
import android.net.Uri
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.helper.rx.BaseComposers
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Named

class FileInteractor @Inject constructor(@Named(APP_CONTEXT) private val context: Context, private val baseComposers: BaseComposers) {
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