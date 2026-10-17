package com.example.citigrow.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }
fun JSONArray.strings(): List<String> = (0 until length()).map { optString(it) }
fun JSONObject.obj(key: String): JSONObject = optJSONObject(key) ?: JSONObject().also { put(key, it) }
fun JSONObject.arr(key: String): JSONArray = optJSONArray(key) ?: JSONArray().also { put(key, it) }
fun JSONObject.text(key: String, fallback: String = ""): String = if (isNull(key)) fallback else optString(key, fallback)
fun JSONObject.copy(): JSONObject = JSONObject(toString())
fun JSONArray.addUnique(value: String) { if (value !in strings()) put(value) }

/** Local demo repository. Updates survive process death; no server sync is claimed. */
class GrowStore(context: Context) {
    private val prefs = context.getSharedPreferences("citigrow_local_v1", Context.MODE_PRIVATE)
    private val source = JSONObject(context.assets.open("prototype.json").bufferedReader().use { it.readText() })
    val data: JSONObject = source.getJSONObject("data")
    private val regionData = JSONObject(context.assets.open("india_regions.json").bufferedReader().use { it.readText() })
    val regions: List<String> = (regionData.getJSONArray("states").strings() + regionData.getJSONArray("unionTerritories").strings()).sorted()
    private val translations = source.getJSONObject("translations").apply {
        put("ml", JSONObject(context.assets.open("translations/ml.json").bufferedReader().use { it.readText() }))
        getJSONObject("en").put("readThisStep", "Read this step")
    }
    var revision by mutableIntStateOf(0)
        private set
    var state: JSONObject = prefs.getString("state", null)?.let { runCatching { JSONObject(it) }.getOrNull() } ?: seed()
        private set
    init { persist() }
    val profiles get() = state.arr("profiles").objects()
    fun list(key: String) = data.arr(key).objects()
    fun find(key: String, id: String) = data.optJSONObject(key)?.optJSONObject(id)
        ?: list(key).firstOrNull { it.text("id") == id } ?: JSONObject()
    fun profile(id: String) = profiles.first { it.text("id") == id }
    fun t(key: String, lang: String = state.text("lang", "en")): String {
        val english = translations.getJSONObject("en")
        val resolved = if (english.has(key)) key else english.keys().asSequence().firstOrNull { english.text(it) == key } ?: key
        return translations.optJSONObject(lang)?.text(resolved)?.takeIf { it.isNotEmpty() } ?: english.text(resolved, key)
    }
    fun change(event: String = "", edit: () -> Unit) {
        edit()
        if (event.isNotEmpty()) state.arr("queue").put(JSONObject().put("id", UUID.randomUUID().toString()).put("event", event).put("at", System.currentTimeMillis()))
        persist()
        revision++
    }
    private fun persist() { prefs.edit().putString("state", state.toString()).apply() }
    fun reset() { state = seed(); persist(); revision++ }
    private fun seed(): JSONObject {
        val students = JSONArray(source.getJSONArray("profiles").toString())
        students.objects().forEach { p ->
            setPin(p, p.text("pin")); p.remove("pin")
            p.put("readAloud", true)
        }
        return JSONObject().put("lang", "en").put("profiles", students).put("queue", JSONArray())
            .put("partnerSubs", JSONArray()).put("modules", JSONArray()).put("recommendations", JSONArray())
            .put("offline", false).put("lite", false)
    }
    fun setPin(p: JSONObject, pin: String) {
        val salt = UUID.randomUUID().toString()
        p.put("pinSalt", salt).put("pinHash", digest(salt + pin))
    }
    fun unlock(p: JSONObject, pin: String): Boolean = MessageDigest.isEqual(
        digest(p.text("pinSalt") + pin).toByteArray(), p.text("pinHash").toByteArray())
    private fun digest(s: String) = MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
}
