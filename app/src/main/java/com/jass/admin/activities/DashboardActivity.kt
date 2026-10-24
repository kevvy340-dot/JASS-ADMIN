package com.jass.admin.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jass.admin.R
import com.jass.admin.adapters.KeysAdapter
import com.jass.admin.network.AdminClient

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvTotal:    TextView
    private lateinit var tvActive:   TextView
    private lateinit var tvExpired:  TextView
    private lateinit var tvBound:    TextView
    private lateinit var recycler:   RecyclerView
    private lateinit var progress:   ProgressBar
    private lateinit var tvEmpty:    TextView
    private lateinit var btnRefresh: ImageButton
    private lateinit var btnGenerate: Button
    private lateinit var filterSpinner: Spinner

    private val keysList = mutableListOf<AdminClient.KeyInfo>()
    private lateinit var adapter: KeysAdapter
    private var allKeys = listOf<AdminClient.KeyInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvTotal    = findViewById(R.id.tvTotal)
        tvActive   = findViewById(R.id.tvActive)
        tvExpired  = findViewById(R.id.tvExpired)
        tvBound    = findViewById(R.id.tvBound)
        recycler   = findViewById(R.id.recyclerKeys)
        progress   = findViewById(R.id.progressDash)
        tvEmpty    = findViewById(R.id.tvEmpty)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnGenerate = findViewById(R.id.btnGenerate)
        filterSpinner = findViewById(R.id.spinnerFilter)

        adapter = KeysAdapter(keysList) { key, pos -> confirmRevoke(key, pos) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Filter spinner
        val filters = arrayOf("All", "Active", "Expired", "Unbound")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filters)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                applyFilter(filters[pos])
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        btnRefresh.setOnClickListener { loadKeys() }
        btnGenerate.setOnClickListener {
            startActivity(Intent(this, GenerateKeyActivity::class.java))
        }

        loadKeys()
    }

    override fun onResume() {
        super.onResume()
        loadKeys()
    }

    private fun loadKeys() {
        progress.visibility = View.VISIBLE
        tvEmpty.visibility  = View.GONE

        AdminClient.listKeys { result ->
            runOnUiThread {
                progress.visibility = View.GONE
                if (result.success) {
                    @Suppress("UNCHECKED_CAST")
                    allKeys = result.data as List<AdminClient.KeyInfo>
                    updateStats(allKeys)
                    applyFilter(filterSpinner.selectedItem.toString())
                } else {
                    tvEmpty.text       = "Error: ${result.message}"
                    tvEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateStats(keys: List<AdminClient.KeyInfo>) {
        val active  = keys.count { !it.expired }
        val expired = keys.count { it.expired }
        val bound   = keys.count { !it.deviceId.isNullOrEmpty() }

        tvTotal.text   = "Total: ${keys.size}"
        tvActive.text  = "Active: $active"
        tvExpired.text = "Expired: $expired"
        tvBound.text   = "Bound: $bound"
    }

    private fun applyFilter(filter: String) {
        val filtered = when (filter) {
            "Active"  -> allKeys.filter { !it.expired }
            "Expired" -> allKeys.filter { it.expired }
            "Unbound" -> allKeys.filter { it.deviceId.isNullOrEmpty() && !it.expired }
            else      -> allKeys
        }
        adapter.updateList(filtered)
        if (filtered.isEmpty()) {
            tvEmpty.text       = "Koi key nahi mili"
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    private fun confirmRevoke(key: String, pos: Int) {
        AlertDialog.Builder(this)
            .setTitle("Revoke Key?")
            .setMessage("$key\n\nYe key permanently revoke ho jaayegi.")
            .setPositiveButton("Revoke") { _, _ -> revokeKey(key, pos) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun revokeKey(key: String, pos: Int) {
        AdminClient.revokeKey(key) { result ->
            runOnUiThread {
                if (result.success) {
                    Toast.makeText(this, "Key revoked", Toast.LENGTH_SHORT).show()
                    loadKeys() // full refresh
                } else {
                    Toast.makeText(this, "Failed: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
