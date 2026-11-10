package com.example.lab8_weather

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

fun AppCompatActivity.applyWindowInsets(root: View) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val initialLeft = root.paddingLeft
    val initialTop = root.paddingTop
    val initialRight = root.paddingRight
    val initialBottom = root.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val safe = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        view.setPadding(initialLeft + safe.left, initialTop + safe.top,
            initialRight + safe.right, initialBottom + safe.bottom)
        insets
    }
    ViewCompat.requestApplyInsets(root)
}
