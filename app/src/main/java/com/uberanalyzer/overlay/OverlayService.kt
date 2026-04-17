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
    override fun onBind(i: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        val catName = i.getStringExtra(EXTRA_CATEGORY) ?: "Uber"
        val cat = com.uberanalyzer.model.RideCategory.fromString(catName)
        
        val dp = { v: Int -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt() }
        val p = WindowManager.LayoutParams(dp(320), -2, if (Build.VERSION.SDK_INT >= 26) 2038 else 2002, 8, -3).apply { gravity = Gravity.TOP; y = 150 }
        
        val pr = i.getDoubleExtra(EXTRA_PRICE, 0.0)
        val km = i.getDoubleExtra(EXTRA_DISTANCE, 0.1)
        val time = i.getIntExtra(EXTRA_TIME, 0)
        
        view = LinearLayout(this).apply {
            orientation = 1; setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable().apply { 
                setColor(Color.parseColor(cat.colorHex))
                cornerRadius = dp(20).toFloat()
                setStroke(dp(4), Color.parseColor(r.colorHex)) 
            }
            
            addView(TextView(context).apply { 
                text = String.format(Locale.getDefault(), "Nota: %.1f | %s", i.getDoubleExtra(EXTRA_SCORE, 0.0), cat.displayName)
                setTextColor(Color.WHITE); textSize = 18f; setTypeface(null, 1)
            })
            
            val pkm = if (km > 0) pr/km else 0.0
            addView(TextView(context).apply { 
                text = String.format(Locale.getDefault(), "R$ %.2f / KM", pkm)
                setTextColor(Color.parseColor(r.colorHex)); textSize = 28f; setTypeface(null, 1)
            })
            
            addView(TextView(context).apply { 
                text = String.format(Locale.getDefault(), "Total: R$ %.2f | %.1f KM", pr, km)
                setTextColor(-3355444); textSize = 16f 
            })
            
            addView(TextView(context).apply { 
                text = String.format(Locale.getDefault(), "Tempo: %d min", time)
                setTextColor(-3355444); textSize = 16f 
            })
        }
        view?.setOnTouchListener { _, _ -> hide(); true }
        try { wm?.addView(view, p) } catch (e: Exception) {}
        Handler(Looper.getMainLooper()).postDelayed({ hide() }, 10000)
    }
    private fun hide() { try { view?.let { wm?.removeView(it); view = null } } catch (e: Exception) {} }
}
