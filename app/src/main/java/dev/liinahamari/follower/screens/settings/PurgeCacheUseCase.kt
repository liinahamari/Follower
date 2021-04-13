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