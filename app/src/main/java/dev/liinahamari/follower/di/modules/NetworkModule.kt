package dev.liinahamari.follower.di.modules

import dev.liinahamari.follower.networking.ServerService
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val LOCALHOST_ENDPOINT = "http://10.0.2.2:8080/"

@Module
class NetworkModule {
    /**FIXME: Temporary solution. � symbol can be changed in a while */
    @Provides
    @Singleton
    fun provideClearLogger(): HttpLoggingInterceptor.Logger = object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            if ("[^�]+".toRegex().matches(message)) {
                Platform.get().log(message, Platform.INFO, null)
            }
        }
    }

    @Provides
    @Singleton
    //todo set NONE in release
    fun provideHttpLoggingInterceptor(logger: HttpLoggingInterceptor.Logger) = HttpLoggingInterceptor(logger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun providesCallAdapterFactory(): CallAdapter.Factory = RxJava2CallAdapterFactory.create()

    @Provides
    @Singleton
    fun providesConverterFactory(): Converter.Factory = GsonConverterFactory.create()

    @Provides
    @Singleton
    fun provideCommonHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideAlbumsService(okHttpClient: OkHttpClient, callAdapterFactory: CallAdapter.Factory, converterFactory: Converter.Factory): ServerService = Retrofit.Builder()
        .addConverterFactory(converterFactory)
        .addCallAdapterFactory(callAdapterFactory)
        .baseUrl(LOCALHOST_ENDPOINT) /*todo: to BuildConfig.ENDPOINT*/
        .client(okHttpClient)
        .build()
        .create(ServerService::class.java)
}