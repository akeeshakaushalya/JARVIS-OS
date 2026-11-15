
package com.jarvis.os

import android.app.Activity
import android.os.Bundle
import android.os.BatteryManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import android.content.pm.PackageManager
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var root: LinearLayout
    private lateinit var info: TextView
    private lateinit var result: TextView
    private lateinit var tts: TextToSpeech

    private val blue = Color.rgb(0, 190, 255)
    private val dark = Color.rgb(3, 12, 30)
    private val lightBlue = Color.rgb(10, 35, 65)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = dark
        window.navigationBarColor = dark

        tts = TextToSpeech(this, this)

        val scrollView = ScrollView(this).apply {
            fillViewport = true
            setBackgroundColor(dark)
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            setPadding(24, 35, 24, 35)
            setBackgroundColor(dark)
        }

        scrollView.addView(root)
        setContentView(scrollView)

        createDashboard()
        updateInfo()
        showApps()
    }

    private fun createDashboard() {

        val title = TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 36f
            setTextColor(blue)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 5)
        }

        val subtitle = TextView(this).apply {
            text = "JUST A RATHER VERY INTELLIGENT SYSTEM"
            textSize = 11f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        info = TextView(this).apply {
            textSize = 19f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(10, 30, 10, 20)
        }

        val circle = TextView(this).apply {
            text = "◉"
            textSize = 95f
            setTextColor(blue)
            gravity = Gravity.CENTER
            setPadding(0, 15, 0, 15)
        }

        val listen = Button(this).apply {
            text = "START VOICE COMMAND"
            textSize = 15f
            isAllCaps = false
            setOnClickListener {
                startListening()
            }
        }

        result = TextView(this).apply {
            text = "JARVIS SYSTEM ONLINE"
            textSize = 15f
            setTextColor(blue)
            gravity = Gravity.CENTER
            setPadding(10, 25, 10, 25)
        }

        val appsTitle = TextView(this).apply {
            text = "APPLICATIONS"
            textSize = 20f
            setTextColor(blue)
            gravity = Gravity.CENTER
            setPadding(0, 25, 0, 15)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(info)
        root.addView(circle)
        root.addView(listen)
        root.addView(result)
        root.addView(appsTitle)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val languageResult = tts.setLanguage(Locale.US)

            if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                result.text = "English voice unavailable"
            }
        }
    }

    private fun speak(message: String) {
        if (::tts.isInitialized) {
            tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "jarvis"
            )
        }
    }

    private fun updateInfo() {
        val time = SimpleDateFormat(
            "hh:mm a", Locale.US
        ).format(Date())

        val date = SimpleDateFormat(
            "EEEE, dd MMMM yyyy", Locale.US
        ).format(Date())

        val batteryManager =
            getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val battery = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY
        )

        info.text = "$time\n$date\nBattery: $battery%"
    }

    private fun startListening() {
        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "en-US"
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Speak to JARVIS"
            )
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

            val command = words?.firstOrNull()
                ?.lowercase(Locale.US) ?: return

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
                val time = SimpleDateFormat(
                    "hh:mm a", Locale.US
                ).format(Date())

                result.text = "Current time is $time"
                speak("The time is $time")
            }

            command.contains("date") -> {
                val date = SimpleDateFormat(
                    "EEEE, dd MMMM yyyy", Locale.US
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
                    startActivity(
                        Intent("android.media.action.IMAGE_CAPTURE")
                    )
                    speak("Opening camera")
                } catch (e: Exception) {
                    result.text = "Camera unavailable"
                }
            }

            command.contains("settings") -> {
                startActivity(
                    Intent(android.provider.Settings.ACTION_SETTINGS)
                )
                speak("Opening settings")
            }

            else -> {
                result.text = "Command not recognized"
                speak("Sorry, I did not understand that command")
            }
        }
    }

    private fun openApp(packageName: String, website: String) {
        val launchIntent =
            packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            startActivity(launchIntent)
            speak("Opening application")
        } else {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse(website)
                )
            )
            speak("Opening website")
        }
    }

    private fun showApps() {

        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val appList = packageManager.queryIntentActivities(
            launcherIntent,
            0
        )

        val sortedApps = appList.distinctBy {
            it.activityInfo.packageName
        }.sortedBy {
            it.loadLabel(packageManager).toString()
        }

        for (app in sortedApps) {

            val name = app.loadLabel(packageManager).toString()

            val button = Button(this).apply {
                text = name
                isAllCaps = false
                textSize = 15f

                setOnClickListener {
                    val launchIntent = packageManager
                        .getLaunchIntentForPackage(
                            app.activityInfo.packageName
                        )

                    if (launchIntent != null) {
                        startActivity(launchIntent)
                    }
                }
            }

            root.addView(
                button,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8
                }
            )
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
