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

package dev.liinahamari.follower.di.modules

import dagger.Module
import dagger.Provides
import dev.liinahamari.follower.BuildConfig
import dev.liinahamari.follower.networking.ServerService
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
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
    fun provideHttpLoggingInterceptor(logger: HttpLoggingInterceptor.Logger) = HttpLoggingInterceptor(logger).apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    @Provides
    @Singleton
    fun providesCallAdapterFactory(): CallAdapter.Factory = RxJava3CallAdapterFactory.create()

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