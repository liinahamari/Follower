package dev.liinahamari.follower.ext

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

fun Activity.handleUsersReactionToPermission(permissionToHandle: String, allPermissions: List<String>, doIfDenied: () -> Unit, doIfAllowed: () -> Unit, doIfNeverAskAgain: () -> Unit) {
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

fun Activity.handleUsersReactionToPermissions(permissionsToHandle: List<String>, allPermissions: List<String>, doIfDenied: () -> Unit, doIfAllowed: () -> Unit, doIfNeverAskAgain: () -> Unit) {
    if (allPermissions.containsAll(permissionsToHandle)) {
        if (permissionsToHandle.any { shouldShowRequestPermissionRationale(it) }) {
            doIfDenied()
        } else {
            if (hasAllPermissions(permissionsToHandle)) {
                doIfAllowed()
            } else {
                doIfNeverAskAgain()
            }
        }
    }
}

fun Fragment.handleUsersReactionToPermissions(permissionsToHandle: List<String>, allPermissions: List<String>, doIfDenied: () -> Unit, doIfAllowed: () -> Unit, doIfNeverAskAgain: () -> Unit)
        = requireActivity().handleUsersReactionToPermissions(permissionsToHandle, allPermissions, doIfDenied, doIfAllowed, doIfNeverAskAgain)

fun Fragment.handleUsersReactionToPermission(permissionToHandle: String, allPermissions: List<String>, doIfDenied: () -> Unit, doIfAllowed: () -> Unit, doIfNeverAskAgain: () -> Unit)
        = requireActivity().handleUsersReactionToPermission(permissionToHandle, allPermissions, doIfDenied, doIfAllowed, doIfNeverAskAgain)

fun Activity.hasPermission(permission: String) = ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
fun Fragment.hasPermission(permission: String) = ActivityCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_GRANTED

fun Context.hasAllPermissions(permissions: List<String>): Boolean = permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
fun Activity.hasAllPermissions(permissions: List<String>): Boolean = permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
fun Fragment.hasAllPermissions(permissions: List<String>): Boolean = permissions.all { ActivityCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }

