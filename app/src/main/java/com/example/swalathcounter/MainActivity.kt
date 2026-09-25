package com.example.swalathcounter

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.swalathcounter.ui.SwalathScreen

class MainActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences
    private var currentCount by mutableIntStateOf(0)
    private var totalCount by mutableIntStateOf(0)
    private var target by mutableIntStateOf(33)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("swalath_prefs", Context.MODE_PRIVATE)

        currentCount = prefs.getInt("current_count", 0)
        totalCount = prefs.getInt("total_count", 0)
        target = prefs.getInt("target", 33)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF10B981),
                    background = Color(0xFF0F172A),
                    surface = Color(0xFF1E293B)
                )
            ) {
                SwalathScreen(
                    count = currentCount,
                    total = totalCount,
                    target = target,
                    onIncrement = { incrementCount() },
                    onResetSession = { resetSession() },
                    onUpdateTarget = { newTarget ->
                        target = newTarget
                        prefs.edit().putInt("target", newTarget).apply()
                    }
                )
            }
        }
    }

    private fun incrementCount() {
        currentCount++
        totalCount++

        prefs.edit()
            .putInt("current_count", currentCount)
            .putInt("total_count", totalCount)
            .apply()

        triggerVibration(isTargetReached = (currentCount % target == 0))
    }

    private fun resetSession() {
        currentCount = 0
        prefs.edit().putInt("current_count", 0).apply()
        triggerLightClick()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                incrementCount()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> true
            else -> super.onKeyUp(keyCode, event)
        }
    }

    private fun triggerVibration(isTargetReached: Boolean) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isTargetReached) {
                val timings = longArrayOf(0, 100, 80, 150)
                val amplitudes = intArrayOf(0, 255, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(if (isTargetReached) 250L else 40L)
        }
    }

    private fun triggerLightClick() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, 100))
        }
    }
}
