package com.example.app1.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class LocationUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val lat = intent?.getStringExtra("latitude") ?: "unknown"
        val long = intent?.getStringExtra("longitude") ?: "unknown"

        // Ovdje možete dodati logiku za ažuriranje UI ili slanje obaveštenja
        Log.d("LocationUpdateReceiver", "Location: ($lat, $long)")

        Toast.makeText(context, "Location updated: ($lat, $long)", Toast.LENGTH_SHORT).show()
    }
}