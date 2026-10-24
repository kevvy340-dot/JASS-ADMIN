package com.jass.admin.network

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object AdminClient {

    private var SERVER = "http://16.176.162.144:3000"
    private var ADMIN_PASS = ""
    private val client = OkHttpClient()
    private val JSON_TYPE = MediaType.parse("application/json; charset=utf-8")

    fun init(serverUrl: String, password: String) {
        SERVER = serverUrl.trimEnd('/')
        ADMIN_PASS = password
    }

    data class KeyInfo(
        val key: String,
        val expiry: String,
        val deviceId: String?,
        val expired: Boolean,
        val createdAt: String
    )

    data class AdminResult(
        val success: Boolean,
        val message: String = "",
        val data: Any? = null
    )

    fun ping(callback: (AdminResult) -> Unit) {
        val request = Request.Builder()
            .url("$SERVER/admin/keys?adminPassword=$ADMIN_PASS")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(AdminResult(false, "Server unreachable: ${e.message}"))
            }
            override fun onResponse(call: Call, response: Response) {
                when (response.code()) {
                    200  -> callback(AdminResult(true, "Connected"))
                    403  -> callback(AdminResult(false, "Wrong password"))
                    else -> callback(AdminResult(false, "HTTP ${response.code()}"))
                }
            }
        })
    }

    fun listKeys(callback: (AdminResult) -> Unit) {
        val request = Request.Builder()
            .url("$SERVER/admin/keys?adminPassword=$ADMIN_PASS")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(AdminResult(false, "Network error: ${e.message}"))
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 403) {
                    callback(AdminResult(false, "Unauthorized")); return
                }
                try {
                    val arr = JSONArray(response.body()!!.string())
                    val list = mutableListOf<KeyInfo>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(KeyInfo(
                            key       = obj.getString("key"),
                            expiry    = obj.getString("expiry"),
                            deviceId  = obj.optString("deviceId", null),
                            expired   = obj.getBoolean("expired"),
                            createdAt = obj.getString("createdAt")
                        ))
                    }
                    callback(AdminResult(true, "OK", list))
                } catch (e: Exception) {
                    callback(AdminResult(false, "Parse error: ${e.message}"))
                }
            }
        })
    }

    fun generateKey(days: Int, features: List<String>, callback: (AdminResult) -> Unit) {
        val body = JSONObject()
            .put("adminPassword", ADMIN_PASS)
            .put("days", days)
            .put("features", JSONArray(features))
            .toString()

        val request = Request.Builder()
            .url("$SERVER/admin/generate")
            .post(RequestBody.create(JSON_TYPE, body))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(AdminResult(false, "Network error: ${e.message}"))
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 403) {
                    callback(AdminResult(false, "Unauthorized")); return
                }
                try {
                    val json = JSONObject(response.body()!!.string())
                    callback(AdminResult(true, json.getString("key"), json))
                } catch (e: Exception) {
                    callback(AdminResult(false, "Parse error"))
                }
            }
        })
    }

    fun revokeKey(key: String, callback: (AdminResult) -> Unit) {
        val body = JSONObject()
            .put("adminPassword", ADMIN_PASS)
            .put("key", key)
            .toString()

        val request = Request.Builder()
            .url("$SERVER/admin/revoke")
            .post(RequestBody.create(JSON_TYPE, body))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(AdminResult(false, "Network error: ${e.message}"))
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 403) {
                    callback(AdminResult(false, "Unauthorized")); return
                }
                try {
                    val json = JSONObject(response.body()!!.string())
                    if (json.optBoolean("revoked", false))
                        callback(AdminResult(true, "Key revoked"))
                    else
                        callback(AdminResult(false, "Revoke failed"))
                } catch (e: Exception) {
                    callback(AdminResult(false, "Parse error"))
                }
            }
        })
    }
}
