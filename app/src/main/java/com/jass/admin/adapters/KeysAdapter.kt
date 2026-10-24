package com.jass.admin.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jass.admin.R
import com.jass.admin.network.AdminClient

class KeysAdapter(
    private val keys: MutableList<AdminClient.KeyInfo>,
    private val onRevoke: (String, Int) -> Unit
) : RecyclerView.Adapter<KeysAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvKey:     TextView = v.findViewById(R.id.tvKey)
        val tvExpiry:  TextView = v.findViewById(R.id.tvExpiry)
        val tvDevice:  TextView = v.findViewById(R.id.tvDevice)
        val tvStatus:  TextView = v.findViewById(R.id.tvStatus)
        val btnRevoke: Button   = v.findViewById(R.id.btnRevoke)
        val btnCopy:   Button   = v.findViewById(R.id.btnCopy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_key, parent, false)
        return VH(v)
    }

    override fun getItemCount() = keys.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val k = keys[position]
        holder.tvKey.text    = k.key
        holder.tvExpiry.text = "Expiry: ${k.expiry.take(10)}"
        holder.tvDevice.text = if (k.deviceId.isNullOrEmpty())
            "Device: Not bound yet"
        else
            "Device: ${k.deviceId.take(16)}..."

        if (k.expired) {
            holder.tvStatus.text = "EXPIRED"
            holder.tvStatus.setTextColor(Color.parseColor("#FF4444"))
            holder.btnRevoke.isEnabled = false
        } else {
            holder.tvStatus.text = "ACTIVE"
            holder.tvStatus.setTextColor(Color.parseColor("#44FF88"))
            holder.btnRevoke.isEnabled = true
        }

        holder.btnRevoke.setOnClickListener { onRevoke(k.key, holder.adapterPosition) }
        holder.btnCopy.setOnClickListener {
            val cm = holder.itemView.context
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("JASS Key", k.key))
            Toast.makeText(holder.itemView.context, "Key copied!", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeAt(pos: Int) {
        keys.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun updateList(newKeys: List<AdminClient.KeyInfo>) {
        keys.clear()
        keys.addAll(newKeys)
        notifyDataSetChanged()
    }
}
