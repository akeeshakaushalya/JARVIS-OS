
package com.jarvis.os

import android.app.Activity
import android.os.Bundle
import android.os.BatteryManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var root: LinearLayout
    private lateinit var info: TextView
    private lateinit var result: TextView
    private lateinit var tts: TextToSpeech

    private val blue = Color.rgb(0, 180, 255)
    private val dark = Color.rgb(3, 12, 30)
    private val apps = mutableListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = dark
        window.navigationBarColor = dark

        tts = TextToSpeech(this, this)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 30, 24, 24)
            setBackgroundColor(dark)
        }

        val title = TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 34f
            setTextColor(blue)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "JUST A RATHER VERY INTELLIGENT SYSTEM"
            textSize = 11f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        info = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(10, 30, 10, 20)
        }

        val circle = TextView(this).apply {
            text = "◉"
            textSize = 100f
            setTextColor(blue)
            gravity = Gravity.CENTER
        }

        val listen = Button(this).apply {
            text = "START VOICE COMMAND"
            setOnClickListener { startListening() }
        }

        result = TextView(this).apply {
            text = "JARVIS SYSTEM ONLINE"
            textSize = 15f
            setTextColor(blue)
            gravity = Gravity.CENTER
            setPadding(10, 20, 10, 20)
        }

        val appsTitle = TextView(this).apply {
            text = "APPLICATIONS"
            textSize = 18f
            setTextColor(blue)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 10)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(info)
        root.addView(circle)
        root.addView(listen)
        root.addView(result)
        root.addView(appsTitle)

        setContentView(root)

        updateInfo()
        showApps()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    private fun speak(message: String) {
        if (::tts.isInitialized) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis")
        }
    }

    private fun updateInfo() {
        val time = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        val date = SimpleDateFormat("EEEE, dd MMMM", Locale.US).format(Date())

        val batteryManager =
            getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val battery = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY
        )

        info.text = "$time\n$date\nBattery: $battery%"
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS")
        }

        try {
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            result.text = "Speech recognition unavailable"
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val words = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )

            val command = words?.firstOrNull()?.lowercase(Locale.US) ?: return

            result.text = "YOU: $command"
            handleCommand(command)
        }
    }

    private fun handleCommand(command: String) {
        when {
            command.contains("hello") -> {
                result.text = "Hello! How can I help you?"
                speak("Hello! How can I help you?")
            }

            command.contains("time") -> {
                updateInfo()
                val time = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
                result.text = "Current time is $time"
                speak("The time is $time")
            }

            command.contains("date") -> {
                val date = SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale.US
                ).format(Date())
                result.text = date
                speak("Today is $date")
            }

            command.contains("battery") -> {
                val batteryManager =
                    getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val battery = batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )
                result.text = "Battery: $battery percent"
                speak("Battery level is $battery percent")
            }

            command.contains("youtube") -> openApp(
                "com.google.android.youtube",
                "https://www.youtube.com"
            )

            command.contains("whatsapp") -> openApp(
                "com.whatsapp",
                "https://www.whatsapp.com"
            )

            command.contains("camera") -> {
                try {
                    startActivity(Intent("android.media.action.IMAGE_CAPTURE"))
                    speak("Opening camera")
                } catch (e: Exception) {
                    result.text = "Camera unavailable"
                }
            }

            command.contains("settings") -> {
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                speak("Opening settings")
            }

            else -> {
                result.text = "Command not recognized"
                speak("Sorry, I did not understand that command")
            }
        }
    }

    private fun openApp(packageName: String, website: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            startActivity(launchIntent)
            speak("Opening application")
        } else {
            startActivity(
                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(website))
            )
            speak("Opening website")
        }
    }

    private fun showApps() {
        val appList = packageManager.getInstalledApplications(
            PackageManager.GET_META_DATA
        )

        val heading = TextView(this).apply {
            text = "Tap an app to open it"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        root.addView(heading)

        for (app in appList) {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                ?: continue

            val name = packageManager.getApplicationLabel(app).toString()

            val button = Button(this).apply {
                text = name
                isAllCaps = false
                setOnClickListener {
                    startActivity(launchIntent)
                }
            }

            root.addView(button)
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
