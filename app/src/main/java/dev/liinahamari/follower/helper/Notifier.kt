/*
 * Copyright 2020-2021 liinahamari
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.liinahamari.follower.helper

import android.content.ContentResolver
import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Vibrator
import android.provider.Settings
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class Notifier @Inject constructor(
    @JvmField val cameraManager: CameraManager? = null,
    private val vibrator: Vibrator?,
    private val audioManager: AudioManager?,
    private val deviceHasFlashFeature: Boolean, /*todo named injection*/
    private val context: Context,
    private val contentResolver: ContentResolver,
) {
    private val disposable = CompositeDisposable()
    private var player: MediaPlayer? = null
    private val vibrationPattern = longArrayOf(0, 300, 300, 300)
    private var stroboscopeOn = false

    fun stop() {
        vibrator?.cancel()
        player?.release()
        player = null
        disposable.clear()
    }

    fun start() {
        stop()
        ring()
        enableStroboscope()
    }

    @Suppress("DEPRECATION")
    private fun turnFlashLight(on: Boolean) = cameraManager?.setTorchMode(cameraManager.cameraIdList[0], on)

    /*FIXME not working on some phones*/
    private fun enableStroboscope() {
        if (deviceHasFlashFeature) {
            disposable += Observable.interval(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .doOnError { turnFlashLight(false) }
                .doOnDispose { turnFlashLight(false) }
                .subscribe {
                    turnFlashLight(stroboscopeOn)
                    stroboscopeOn = stroboscopeOn.not()
                }
        }
    }

    /*FIXME not working on some phones*/
    private fun createPlayer(): MediaPlayer? = try {
        MediaPlayer().apply {
            /*todo run if\else in case of call, DND, smth else?*/
            setDataSource(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            isLooping = true
            setAudioStreamType(AudioManager.STREAM_RING)
        }
    } catch (e: IOException) {
        null.also {
            FlightRecorder.w { "Failed to create player for incoming call ringer" }
        }
    }

    private fun ring() {
        kotlin.runCatching {
            Settings.Global.getInt(contentResolver, "zen_mode")
        }.onSuccess {
            if (it != 1) {
                player = createPlayer()?.apply {
                    try {
                        if (isPlaying.not()) {
                            prepare()
                            start()
                            FlightRecorder.i { "Playing ringtone now." }
                        } else {
                            FlightRecorder.w { "Ringtone is already playing." }
                        }
                    } catch (e: IllegalStateException) {
                        FlightRecorder.e(label = "Notifier.ring()", e)
                    } catch (e: IOException) {
                        FlightRecorder.e(label = "Notifier.ring(), IOE", e)
                    }
                }
                if (audioManager?.ringerMode != AudioManager.RINGER_MODE_SILENT) {
                    vibrator?.vibrate(vibrationPattern, 0)
                }
            }
        }
    }
}