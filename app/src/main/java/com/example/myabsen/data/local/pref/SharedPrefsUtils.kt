package com.example.myabsen.data.local.pref

import android.content.Context
import android.content.SharedPreferences

object SharedPrefsUtils {
    private const val PREFS_NAME = "MyAbsenPrefs"
    private const val CAMERA_CLICK_COUNT = "camera_click_count"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCameraClickCount(context: Context, nip: String): Int {
        return getPrefs(context).getInt("$CAMERA_CLICK_COUNT-$nip", 0)
    }

    fun incrementCameraClickCount(context: Context, nip: String) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        val count = getCameraClickCount(context, nip)
        editor.putInt("$CAMERA_CLICK_COUNT-$nip", count + 1)
        editor.apply()
    }

    fun resetCameraClickCount(context: Context, nip: String) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        editor.putInt("$CAMERA_CLICK_COUNT-$nip", 0)
        editor.apply()
    }
}
