
package com.jarvis.os

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(3, 12, 30)
        window.navigationBarColor = Color.rgb(3, 12, 30)

        val screen = TextView(this).apply {
            text = "J.A.R.V.I.S\n\nSYSTEM ONLINE\n\nWelcome, Sir."
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(0, 190, 255))
            setBackgroundColor(Color.rgb(3, 12, 30))
        }

        setContentView(screen)
    }
}
