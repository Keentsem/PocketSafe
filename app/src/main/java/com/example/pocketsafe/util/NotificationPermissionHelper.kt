package com.example.pocketsafe.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Helper class to handle notification permission requests for Android 13+ (API 33+)
 * Maintains the pixel-retro theme styling of the PocketSafe app
 */
object NotificationPermissionHelper {
    private const val TAG = "NotificationPermHelper"

    /**
     * Checks if the app has notification permission
     * @param context The context to check permissions in
     * @return true if permission is granted or not needed (Android < 13), false otherwise
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Permission not required for Android versions below 13
            true
        }
    }

    /**
     * Registers a permission request launcher in an activity
     * Call this in onCreate() of the activity
     * @param activity The activity that will handle the permission request
     * @param onPermissionResult Callback with the result of the permission request
     * @return The permission launcher to be stored as a property in the activity
     */
    fun registerForPermissionResult(
        activity: AppCompatActivity,
        onPermissionResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            Log.d(TAG, "Notification permission granted: $isGranted")
            onPermissionResult(isGranted)
        }
    }

    /**
     * Request notification permission if needed
     * @param activity The activity requesting the permission
     * @param permissionLauncher The launcher registered with registerForPermissionResult
     * @return true if permission is already granted or not needed, false if request was made
     */
    fun requestNotificationPermissionIfNeeded(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<String>
    ): Boolean {
        // Only request permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Requesting notification permission")
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return false
            }
        }
        // Permission already granted or not needed
        return true
    }
}
