package com.jass.admin.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.jass.admin.R
import com.jass.admin.network.AdminClient

class GenerateKeyActivity : AppCompatActivity() {

    private lateinit var etDays:       EditText
    private lateinit var cbEsp:        CheckBox
    private lateinit var cbAimbot:     CheckBox
    private lateinit var cbSpeedhack:  CheckBox
    private lateinit var btnGenerate:  Button
    private lateinit var tvResult:     TextView
    private lateinit var btnCopy:      Button
    private lateinit var btnCopyShare: Button
    private lateinit var progress:     ProgressBar
    private lateinit var cardResult:   View

    private var generatedKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_key)

        etDays       = findViewById(R.id.etDays)
        cbEsp        = findViewById(R.id.cbEsp)
        cbAimbot     = findViewById(R.id.cbAimbot)
        cbSpeedhack  = findViewById(R.id.cbSpeedhack)
        btnGenerate  = findViewById(R.id.btnGenerateKey)
        tvResult     = findViewById(R.id.tvGeneratedKey)
        btnCopy      = findViewById(R.id.btnCopyKey)
        btnCopyShare = findViewById(R.id.btnShareKey)
        progress     = findViewById(R.id.progressGenerate)
        cardResult   = findViewById(R.id.cardResult)

        cardResult.visibility = View.GONE

package com.jass.admin.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.jass.admin.R
import com.jass.admin.network.AdminClient

class GenerateKeyActivity : AppCompatActivity() {

    private lateinit var etDays:       EditText
    private lateinit var cbEsp:        CheckBox
    private lateinit var cbAimbot:     CheckBox
    private lateinit var cbSpeedhack:  CheckBox
    private lateinit var btnGenerate:  Button
    private lateinit var tvResult:     TextView
    private lateinit var btnCopy:      Button
    private lateinit var btnCopyShare: Button
    private lateinit var progress:     ProgressBar
    private lateinit var cardResult:   View

    private var generatedKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_key)

        etDays       = findViewById(R.id.etDays)
        cbEsp        = findViewById(R.id.cbEsp)
        cbAimbot     = findViewById(R.id.cbAimbot)
        cbSpeedhack  = findViewById(R.id.cbSpeedhack)
        btnGenerate  = findViewById(R.id.btnGenerateKey)
        tvResult     = findViewById(R.id.tvGeneratedKey)
        btnCopy      = findViewById(R.id.btnCopyKey)
        btnCopyShare = findViewById(R.id.btnShareKey)
        progress     = findViewById(R.id.progressGenerate)
        cardResult   = findViewById(R.id.cardResult)

        cardResult.visibility = View.GONE
        cbEsp.isChecked       = true
        cbAimbot.isChecked    = true
        cbSpeedhack.isChecked = true

        btnGenerate.setOnClickListener { generateKey() }

        btnCopy.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("JASS Key", generatedKey))
            Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
        }

        btnCopyShare.setOnClickListener {
            val shareMsg = "JASS Key: $generatedKey\nDownload JASS and activate your key."
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("JASS Key Share", shareMsg))
            Toast.makeText(this, "Share message copied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateKey() {
        val daysStr = etDays.text.toString().trim()
        if (daysStr.isEmpty()) { etDays.error = "Days daalo"; return }
        val days = daysStr.toIntOrNull()
        if (days == null || days <= 0) { etDays.error = "Valid number daalo"; return }

        val features = mutableListOf<String>()
        if (cbEsp.isChecked)       features.add("esp")
        if (cbAimbot.isChecked)    features.add("aimbot")
        if (cbSpeedhack.isChecked) features.add("speedhack")

        if (features.isEmpty()) {
            Toast.makeText(this, "Kam se kam ek feature select karo", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        cardResult.visibility = View.GONE

        AdminClient.generateKey(days, features) { result ->
            runOnUiThread {
                showLoading(false)
                if (result.success) {
                    generatedKey          = result.message
                    tvResult.text         = generatedKey
                    cardResult.visibility = View.VISIBLE
                    Toast.makeText(this, "Key generated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progress.visibility   = if (show) View.VISIBLE else View.GONE
        btnGenerate.isEnabled = !show
    }
}
isEnabled
      = !show
    }
}
