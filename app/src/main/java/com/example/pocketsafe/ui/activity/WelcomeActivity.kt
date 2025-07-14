package com.example.pocketsafe.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketsafe.ui.auth.LoginActivity
import com.example.pocketsafe.R
import com.example.pocketsafe.util.NotificationPermissionHelper

class WelcomeActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "PocketSafePrefs"
    private val KEY_FIRST_LAUNCH = "first_launch"
    private val TAG = "WelcomeActivity"
    
    // Permission request launcher
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        // Register for notification permission result
        notificationPermissionLauncher = NotificationPermissionHelper.registerForPermissionResult(this) { isGranted ->
            Log.d(TAG, "Notification permission granted: $isGranted")
            if (isGranted) {
                Toast.makeText(this, "Thank you! You'll receive bill and subscription reminders.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "You won't receive bill reminders. You can enable notifications in settings.", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Show splash screen for a brief moment, then proceed
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            // Always show welcome dialog on launch
            showWelcomeDialog()
        }, 2000) // 2 second delay
    }

    private fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    private fun setFirstLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    private fun showWelcomeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_welcome_message, null)
        val btnGetStarted = dialogView.findViewById<Button>(R.id.btnGetStarted)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        btnGetStarted.setOnClickListener {
            setFirstLaunchComplete()
            dialog.dismiss()
            navigateToMainActivity()
        }
        
        dialog.show()
    }

    private fun navigateToMainActivity() {
        try {
            // Request notification permission on Android 13+ before navigating
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!NotificationPermissionHelper.hasNotificationPermission(this)) {
                    Log.d(TAG, "Requesting notification permission")
                    NotificationPermissionHelper.requestNotificationPermissionIfNeeded(this, notificationPermissionLauncher)
                    // We'll continue to the login screen regardless of permission result
                }
            }
            
            // Navigate to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to main activity: ${e.message}", e)
            // Navigate anyway to prevent getting stuck
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
