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

package dev.liinahamari.follower.screens.settings

import android.content.Context
import dev.liinahamari.follower.helper.rx.BaseComposers
import io.reactivex.rxjava3.core.Observable
import java.io.File

@SettingsScope
class PurgeCacheUseCase constructor(private val context: Context, private val baseComposers: BaseComposers) {
    fun execute(): Observable<PurgeCacheResult> = Observable.fromCallable {
        deleteDir(context.cacheDir) && context.externalCacheDirs.map { deleteDir(it) }.all { it }
    }.map {
        if (it) PurgeCacheResult.Success else PurgeCacheResult.Failure
    }
        .onErrorReturnItem(PurgeCacheResult.Failure)
        .compose(baseComposers.applyObservableSchedulers())
        .startWithItem(PurgeCacheResult.Progress)

    private fun deleteDir(dir: File): Boolean = when {
        dir.isDirectory -> {
            dir.list()?.map {
                deleteDir(File(dir, it))
            }?.all { it } == true && dir.delete()
        }
        dir.isFile -> dir.delete()
        else -> false
    }
}

sealed class PurgeCacheResult {
    object Progress : PurgeCacheResult()
    object Success : PurgeCacheResult()
    object Failure : PurgeCacheResult()
}