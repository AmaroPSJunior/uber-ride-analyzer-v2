package com.uberanalyzer

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.uberanalyzer.settings.SettingsManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var settings: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SettingsManager(this)
        setContentView(buildUI())
    }

    private fun buildUI(): ScrollView {
        val dp = { v: Int -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt() }
        val scrollView = ScrollView(this).apply { setBackgroundColor(Color.parseColor("#121212")); isFillViewport = true }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(20), dp(20), dp(20)) }

        root.addView(TextView(this).apply { text = "Configurações de Análise"; setTextColor(Color.WHITE); textSize = 20f; typeface = Typeface.DEFAULT_BOLD; setPadding(0, 0, 0, dp(20)) })

        // Thresholds
        root.addView(createLabel("Meta R$ / KM (ex: 2.0)"))
        val kmInput = createEditText(settings.getMinKmValue().toString(), InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        root.addView(kmInput)

        root.addView(createLabel("Meta R$ / Hora (ex: 45.0)"))
        val hourInput = createEditText(settings.getMinHourValue().toString(), InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        root.addView(hourInput)

        root.addView(TextView(this).apply { text = "Cores das Categorias (HEX)"; setTextColor(Color.WHITE); textSize = 18f; setPadding(0, dp(20), 0, dp(10)) })
        
        val xColor = createColorInput("UberX", SettingsManager.KEY_COLOR_UBER_X, SettingsManager.DEFAULT_UBER_X_COLOR)
        root.addView(xColor.first); root.addView(xColor.second)
        
        val comfortColor = createColorInput("Comfort", SettingsManager.KEY_COLOR_COMFORT, SettingsManager.DEFAULT_COMFORT_COLOR)
        root.addView(comfortColor.first); root.addView(comfortColor.second)
        
        val blackColor = createColorInput("Black", SettingsManager.KEY_COLOR_BLACK, SettingsManager.DEFAULT_BLACK_COLOR)
        root.addView(blackColor.first); root.addView(blackColor.second)
        
        val flashColor = createColorInput("Flash", SettingsManager.KEY_COLOR_FLASH, SettingsManager.DEFAULT_FLASH_COLOR)
        root.addView(flashColor.first); root.addView(flashColor.second)

        root.addView(TextView(this).apply { text = "Cores das Avaliações (HEX)"; setTextColor(Color.WHITE); textSize = 18f; setPadding(0, dp(20), 0, dp(10)) })
        
        val excelentColor = createColorInput("Excelente", SettingsManager.KEY_COLOR_EXCELLENT, SettingsManager.DEFAULT_EXCELLENT_COLOR)
        root.addView(excelentColor.first); root.addView(excelentColor.second)

        val goodColor = createColorInput("Boa", SettingsManager.KEY_COLOR_GOOD, SettingsManager.DEFAULT_GOOD_COLOR)
        root.addView(goodColor.first); root.addView(goodColor.second)

        val averageColor = createColorInput("Média/OK", SettingsManager.KEY_COLOR_AVERAGE, SettingsManager.DEFAULT_AVERAGE_COLOR)
        root.addView(averageColor.first); root.addView(averageColor.second)
        
        val badColor = createColorInput("Ruim", SettingsManager.KEY_COLOR_BAD, SettingsManager.DEFAULT_BAD_COLOR)
        root.addView(badColor.first); root.addView(badColor.second)

        // Save Button
        val saveBtn = Button(this).apply {
            text = "SALVAR CONFIGURAÇÕES"; setBackgroundColor(Color.parseColor("#4CAF50")); setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(30), 0, dp(40)) }
            setOnClickListener {
                settings.setMinKmValue(kmInput.text.toString().toFloatOrNull() ?: 2.0f)
                settings.setMinHourValue(hourInput.text.toString().toFloatOrNull() ?: 45.0f)
                
                settings.setCategoryColor(SettingsManager.KEY_COLOR_UBER_X, xColor.second.text.toString())
                settings.setCategoryColor(SettingsManager.KEY_COLOR_COMFORT, comfortColor.second.text.toString())
                settings.setCategoryColor(SettingsManager.KEY_COLOR_BLACK, blackColor.second.text.toString())
                settings.setCategoryColor(SettingsManager.KEY_COLOR_FLASH, flashColor.second.text.toString())
                
                settings.setRatingColor(SettingsManager.KEY_COLOR_EXCELLENT, excelentColor.second.text.toString())
                settings.setRatingColor(SettingsManager.KEY_COLOR_GOOD, goodColor.second.text.toString())
                settings.setRatingColor(SettingsManager.KEY_COLOR_AVERAGE, averageColor.second.text.toString())
                settings.setRatingColor(SettingsManager.KEY_COLOR_BAD, badColor.second.text.toString())
                
                Toast.makeText(this@SettingsActivity, "Configurações Salvas!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        root.addView(saveBtn)

        scrollView.addView(root)
        return scrollView
    }

    private fun createLabel(text: String) = TextView(this).apply { 
        this.text = text; setTextColor(Color.LTGRAY); setPadding(0, 10, 0, 5) 
    }

    private fun createEditText(value: String, inputType: Int) = EditText(this).apply {
        setText(value); setTextColor(Color.WHITE); setBackgroundColor(Color.parseColor("#2C2C2C"))
        this.inputType = inputType; setPadding(20, 20, 20, 20)
    }

    private fun createColorInput(label: String, key: String, default: String): Pair<TextView, EditText> {
        val labelView = createLabel(label)
        val input = createEditText(settings.getCategoryColor(key, default), InputType.TYPE_CLASS_TEXT)
        return Pair(labelView, input)
    }
}
