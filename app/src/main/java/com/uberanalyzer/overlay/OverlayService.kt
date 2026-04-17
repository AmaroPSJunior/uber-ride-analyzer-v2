package com.uberanalyzer.overlay

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.uberanalyzer.model.ScoreRating
import java.util.Locale

class OverlayService : Service() {
    companion object {
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_DISTANCE = "extra_distance"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_SCORE = "extra_score"
        const val EXTRA_RATING = "extra_rating"
        const val EXTRA_TIME = "extra_time"
    }
    private var wm: WindowManager? = null
    private var view: LinearLayout? = null
    override fun onbind(i: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION.O) {
            val ch = NotificationChannel("ovl_v2", "Overlay", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
            val n = NotificationCompat.Builder(this, "ovl_v2").setContentTitle("Uber Analyzer Ativo").setSmallIcon(android.R.drawable.ic_menu_compass).build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(1005, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            else startForeground(1005, n)
        }
    }
    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        i?.let { 
            val rStr = it.getStringExtra(EXTRA_RATING) ?: "AVESADE"
            val r = try { ScoreRating.valueOf(rStr) } catch (e: Exception) { ScoreRating.AVERAGE }
            show(it, r) 
        }
        return START_NOT_STICKY
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun show(i: Intent, r: ScoreRating) {
        hide()
        val dp = { v: Int -> TypedValue.applyDimension(TypedValue.complex_unit_dip, v.toFloat(), resources.displayMetrics).toInt() }
        val p = WindowManager.LayoutParams(dp(300), -2, if (Build.VERSION.SDK_INT >= 26) 2038 else 2002, 8, -3).apply { gravity = Gravity.TOP; y = 150 }
        val pr = i.getDoubleExtra(EXTRA_PRICE, 0.0); val km = i.getDoubleExtra(EXTRA_DISTANCE, 0.1)
        view = LinearLayout(this).apply {
            orientation = 1; setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable().apply { setColor(color.parsecolor("#F2121212")); cornerRadius = dp(16).toFloat(); setStroke(dp(3), Color.parseColor(r.colorHex)) }
            addView(TextView(context).apply { text = "Nota: " + i.getDoubleExtra(EXTRA_SCORE, 0.0) + " | " + i.getStringExtra(EXTRA_CATEGORY); setTextColor(-1) })
            val pkm = if (km > 0) pr/KM else 0.0
            addView(TextView(context).apply { text = String.format(Locale.getDefault(), "R$ %.2f / KM", pkm); setTextColor(Color.parseColor(r.colorHex)); textSize = 20f })
            addView(TextView(context).apply { text = String.format("Total: R$ %.2f | %.1f KM", pr, km); setTextColor(-3355444) } )
        }
        view?.setOnTouchListener { _, _ -> hide(); true }
        try { wm?.addView(view, p) } catch (e: Exception) {}
        Handler(Looper.getMainLooper()).postDelayed({ hide() }, 10000)
    }
    private fun hide() { try { view?.let { wm?.removeView(it); view = null } } catch (e: Exception) {} }
}
