package com.jass.admin.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tencent.mmkv.MMKV
import com.jass.admin.R
import com.jass.admin.network.AdminClient

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var etServer:   EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin:   Button
    private lateinit var tvError:    TextView
    private lateinit var progress:   ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        MMKV.initialize(this)

        etServer   = findViewById(R.id.etServer)
        etPassword = findViewById(R.id.etPassword)
        btnLogin   = findViewById(R.id.btnAdminLogin)
        tvError    = findViewById(R.id.tvAdminError)
        progress   = findViewById(R.id.progressAdmin)

        val mmkv = MMKV.defaultMMKV()
        // ⚠️ YOUR_VPS_IP yahan change karo:
        val savedServer = mmkv.decodeString("admin_server", "http://16.176.162.144:3000")
        etServer.setText(savedServer)

        val savedPass = mmkv.decodeString("admin_pass", "")
        if (!savedPass.isNullOrEmpty()) {
            etPassword.setText(savedPass)
        }

        btnLogin.setOnClickListener { doLogin() }
    }

    private fun doLogin() {
        val server = etServer.text.toString().trim()
        val pass   = etPassword.text.toString().trim()

        if (server.isEmpty() || pass.isEmpty()) {
            showError("Server URL aur password dono daalo")
            return
        }

        showLoading(true)
        tvError.visibility = View.GONE

        AdminClient.init(server, pass)
        AdminClient.ping { result ->
            runOnUiThread {
                showLoading(false)
                if (result.success) {
                    MMKV.defaultMMKV().encode("admin_server", server)
                    MMKV.defaultMMKV().encode("admin_pass",   pass)
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    showError(result.message)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled  = !show
    }

    private fun showError(msg: String) {
        tvError.text       = msg
        tvError.visibility = View.VISIBLE
    }
}
