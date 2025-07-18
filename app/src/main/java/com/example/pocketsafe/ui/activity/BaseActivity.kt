package com.example.pocketsafe.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketsafe.R

/**
 * BaseActivity that handles common functionality across all activities
 * including the navigation bar setup
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Call this method after setContentView in child activities to setup the navigation bar
     */
    protected fun setupNavigationBar() {
        try {
            // Find the navigation bar container from the activity layout
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            if (rootView == null || rootView.childCount == 0) {
                Log.e("BaseActivity", "Root view is null or has no children")
                return
            }
            
            val contentView = rootView.getChildAt(0) as? ViewGroup
            if (contentView == null) {
                Log.e("BaseActivity", "Content view is not a ViewGroup")
                return
            }
            
            // Check if navigation bar already exists to prevent duplicates
            val existingNavBar = contentView.findViewById<LinearLayout>(R.id.navigationBar)
            if (existingNavBar != null) {
                Log.d("BaseActivity", "Navigation bar already exists, skipping setup")
                return
            }
            
            // Inflate the navigation bar
            try {
                val navigationBar = layoutInflater.inflate(R.layout.bottom_navigation_bar, null) as LinearLayout
                
                // Add navigation bar to the bottom of the root view
                val layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                contentView.addView(navigationBar, layoutParams)
                
                // Set up Home button
                navigationBar.findViewById<LinearLayout>(R.id.btnHomeNav)?.setOnClickListener {
                    // Navigate to Main Menu
                    navigateToActivity("com.example.pocketsafe.ui.MainMenu")
                }
                
                // Set up Banking button
                navigationBar.findViewById<LinearLayout>(R.id.btnBankingNav)?.setOnClickListener {
                    // Navigate to Banking Details
                    navigateToActivity("com.example.pocketsafe.ui.activity.BankingActivity")
                }
                
                // Set up Analytics button
                navigationBar.findViewById<LinearLayout>(R.id.btnAnalyticsNav)?.setOnClickListener {
                    // Navigate to Analytics/Money view
                    navigateToActivity("com.example.pocketsafe.ViewExpensesActivity")
                }
            } catch (e: Exception) {
                Log.e("BaseActivity", "Error inflating navigation bar: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e("BaseActivity", "Error setting up navigation: ${e.message}", e)
            Toast.makeText(this, "Error setting up navigation", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Navigate to an activity using its class name
     */
    private fun navigateToActivity(className: String) {
        try {
            val currentClass = this.javaClass.name
            // Don't navigate if we're already on this screen
            if (currentClass == className) return
            
            Log.d("BaseActivity", "Attempting to navigate to: $className")
            
            try {
                val activityClass = Class.forName(className)
                val intent = Intent(this, activityClass)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP) // Prevent multiple instances
                startActivity(intent)
            } catch (classException: ClassNotFoundException) {
                Log.e("BaseActivity", "Activity class not found: $className", classException)
                Toast.makeText(this, "Navigation error: Activity not found", Toast.LENGTH_SHORT).show()
            } catch (intentException: Exception) {
                Log.e("BaseActivity", "Error creating intent for $className: ${intentException.message}", intentException)
                Toast.makeText(this, "Navigation error: Could not start activity", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("BaseActivity", "Error navigating to $className: ${e.message}", e)
            Toast.makeText(this, "Navigation error", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Show a help dialog for the savings dial feature
     */
    protected fun showDialHelpDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_dial_help, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<android.widget.Button>(R.id.btnGotIt).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
