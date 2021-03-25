package com.example.follower.di.modules

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.follower.workers.AutoStartTrackingWorker
import com.example.follower.workers.AutoStopTrackingWorker
import com.example.follower.workers.UploadTrackWorker
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

@Module
interface WorkersBindingModule {
    @Binds
    @IntoMap
    @WorkerKey(UploadTrackWorker::class)
    fun bindSyncTrackWorker(factory: UploadTrackWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(AutoStartTrackingWorker::class)
    fun bindAutoStartTrackingWorker(factory: AutoStartTrackingWorker.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(AutoStopTrackingWorker::class)
    fun bindAutoStopTrackingWorker(factory: AutoStopTrackingWorker.Factory): ChildWorkerFactory

    @Binds
    fun bindAlbumsWorkerFactory(factory: FollowerWorkersFactory): WorkerFactory
}

@MapKey
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerKey(val value: KClass<out ListenableWorker>)

interface ChildWorkerFactory {
    fun create(appContext: Context, params: WorkerParameters): ListenableWorker
}

class FollowerWorkersFactory @Inject constructor(private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory>>) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker {
        val factoryProvider = workerFactories.entries.find { Class.forName(workerClassName).isAssignableFrom(it.key) }?.value ?: throw IllegalArgumentException("Unknown worker class name: $workerClassName")
        return factoryProvider.get().create(appContext, workerParameters)
    }
}