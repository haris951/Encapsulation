package com.example.geozilla

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class NotificationActivity : AppCompatActivity() {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchArriving: Switch
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchLeaving: Switch
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        val userName = intent.getStringExtra("USER_NAME")
        findViewById<TextView>(R.id.usernamenotification).text = userName

        switchArriving = findViewById(R.id.switch1)
        switchLeaving = findViewById(R.id.switch2)

        sharedPreferences = getSharedPreferences("GeozillaPrefs", Context.MODE_PRIVATE)

        switchArriving.isChecked = sharedPreferences.getBoolean("arriving_notification", false)
        switchLeaving.isChecked = sharedPreferences.getBoolean("leaving_notification", false)

        // Set up listeners for the switches
        switchArriving.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                handleArrivingNotificationEnabled()
            } else {
                handleArrivingNotificationDisabled()
            }
        }

        switchLeaving.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                handleLeavingNotificationEnabled()
            } else {
                handleLeavingNotificationDisabled()
            }
        }
    }

    private fun handleArrivingNotificationEnabled() {
        sharedPreferences.edit().putBoolean("arriving_notification", true).apply()
        Toast.makeText(this, "Arriving notification enabled", Toast.LENGTH_SHORT).show()
    }

    private fun handleArrivingNotificationDisabled() {
        sharedPreferences.edit().putBoolean("arriving_notification", false).apply()
        Toast.makeText(this, "Arriving notification disabled", Toast.LENGTH_SHORT).show()
    }

    private fun handleLeavingNotificationEnabled() {
        sharedPreferences.edit().putBoolean("leaving_notification", true).apply()
        Toast.makeText(this, "Leaving notification enabled", Toast.LENGTH_SHORT).show()
    }

    private fun handleLeavingNotificationDisabled() {
        sharedPreferences.edit().putBoolean("leaving_notification", false).apply()
        Toast.makeText(this, "Leaving notification disabled", Toast.LENGTH_SHORT).show()
    }
}
