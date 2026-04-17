package com.uberanalyzer

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.uberanalyzer.service.UberAccessibilityService
import com.uberanalyzer.overlay.OverlayService

class MainActivity : AppCompatActivity() {

    private lateinit var accStatusView: TextView
    private lateinit var overlayStatusView: TextView
    private lateinit var lastLogView: TextView
    private lateinit var accButton: Button
    private lateinit var overlayButton: Button

    private val debugReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val text = intent?.getStringExtra("log_text") ?: ""
            lastLogView.text = "[" + (System.currentTimeMillis() % 10000).toString() + "] " + text
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUI())
        val filter = IntentFilter("DEBUG_LOG")
        // No permission needed for internal app communication, simplifies everything
        registerReceiver(debugReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(debugReceiver)
    }

    override fun onResume() {
        super.onResume()
        updateStatuses()
    }

    private fun updateStatuses() {
        val accEnabled = isAccessibilityServiceEnabled()
        val overlayEnabled = canDrawOverlays()

        accStatusView.text = if (accEnabled) "✅ LEITOR ATIVADO" else "❌ LEITOR DESATIVADO"
        accStatusView.setTextColor(if (accEnabled) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
        accButton.visibility = if (accEnabled) View.GONE else View.VISIBLE

        overlayStatusView.text = if (overlayEnabled) "✅ SOBREPOSIÇÃO OK" else "❌ SOBREPOSIÇÃO BLOQUEADA"
        overlayStatusView.setTextColor(if (overlayEnabled) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
        overlayButton.visibility = if (overlayEnabled) View.GONE else View.VISIBLE
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = ComponentName(this, UberAccessibilityService::class.java).flattenToString()
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        return enabled.contains(expected)
    }

    private fun canDrawOverlays(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this) else true

    private fun buildUI(): View {
        val dp = { v: Int -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt() }
        val scrollView = ScrollView(this).apply { setBackgroundColor(Color.parseColor("#121212")); isFillViewport = true }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(40), dp(20), dp(20)); gravity = Gravity.CENTER_HORIZONTAL }

        root.addView(TextView(this).apply { text = "Uber Analyzer 1.0"; setTextColor(Color.WHITE); textSize = 22f; typeface = Typeface.DEFAULT_BOLD })
        
        // Battery Warning
        root.addView(TextView(this).apply { 
            text = "⚠️ IMPORTANTE: Desative o 'Modo de Economia de Energia' para o app funcionar sempre!"; 
            setTextColor(Color.YELLOW); textSize = 14f; setGravity(Gravity.CENTER)
            setPadding(0, dp(10), 0, dp(10))
        })

        accStatusView = buildStatusView(dp)
        root.addView(accStatusView)
        
        accButton = Button(this).apply { text = "1. Ativar Leitor de Tela"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }
        root.addView(accButton)

        overlayStatusView = buildStatusView(dp)
        root.addView(overlayStatusView)

        overlayButton = Button(this).apply {
            text = "2. Permitir Sobreposição"
            setOnClickListener { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))) }
        }
        root.addView(overlayButton)

        root.addView(Button(this).apply {
            text = "🚀 Testar Popup"; setBackgroundColor(Color.DKGRAY); setTextColor(Color.WHITE)
            setOnClickListener {
                val intent = Intent(this@MainActivity, OverlayService::class.java).apply {
                    putExtra(OverlayService.EXTRA_PRICE, 10.50); putExtra(OverlayService.EXTRA_DISTANCE, 3.5)
                    putExtra(OverlayService.EXTRA_TIME, 8); putExtra(OverlayService.EXTRA_CATEGORY, "Teste")
                    putExtra(OverlayService.EXTRA_SCORE, 9.0); putExtra(OverlayService.EXTRA_RATING, "EXCELLENT")
                }
                startService(intent)
            }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(10), 0, dp(10)) }
        })

        root.addView(TextView(this).apply { text = "🔍 O que o leitor está vendo:"; setTextColor(Color.MAGENTA); setPadding(0, dp(20), 0, dp(5)) })
        lastLogView = TextView(this).apply {
            text = "Aguardando dados da Uber..."; setTextColor(Color.GREEN); setBackgroundColor(Color.BLACK)
            setPadding(dp(10), dp(10), dp(10), dp(10)); layoutParams = LinearLayout.LayoutParams(-1, dp(150))
            textSize = 10f; typeface = Typeface.MONOSPACE
        }
        root.addView(lastLogView)

        scrollView.addView(root)
        return scrollView
    }

    private fun buildStatusView(dp: (Int) -> Int) = TextView(this).apply {
        setPadding(dp(10), dp(10), dp(10), dp(10)); gravity = Gravity.CENTER
        background = GradientDrawable().apply { setColor(Color.parseColor("#1A1A1A")); cornerRadius = dp(8).toFloat() }
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(10), 0, dp(5)) }
    }
}
