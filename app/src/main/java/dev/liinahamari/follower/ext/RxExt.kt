package dev.liinahamari.follower.ext

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/** Only for RxView elements!*/
fun Observable<Unit>.throttleFirst(skipDurationMillis: Long = 500L): Observable<Unit> = compose { it.throttleFirst(skipDurationMillis, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }
