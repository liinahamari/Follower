package com.example.follower

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

fun Activity.handleUsersReactionToPermission(
    permissionToHandle: String,
    allPermissions: Array<out String>,
    doIfDenied: () -> Unit,
    doIfAllowed: () -> Unit,
    doIfNeverAskAgain: () -> Unit
) {
    if (allPermissions.contains(permissionToHandle)) {
        if (shouldShowRequestPermissionRationale(permissionToHandle)) {
            doIfDenied()
        } else {
            if (hasPermission(permissionToHandle)) {
                doIfAllowed()
            } else {
                doIfNeverAskAgain()
            }
        }
    }
}

fun Fragment.handleUsersReactionToPermission(permissionToHandle: String,
                                             allPermissions: Array<out String>,
                                             doIfDenied: () -> Unit,
                                             doIfAllowed: () -> Unit,
                                             doIfNeverAskAgain: () -> Unit) = requireActivity()
    .handleUsersReactionToPermission(permissionToHandle, allPermissions, doIfDenied, doIfAllowed, doIfNeverAskAgain)

fun Activity.hasPermission(permission: String) = ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.hasAllPermissions(permissions: Array<String>): Boolean = permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

/** Only for RxView elements!*/
fun Observable<Unit>.throttleFirst(): Observable<Unit> = compose { it.throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()) }
