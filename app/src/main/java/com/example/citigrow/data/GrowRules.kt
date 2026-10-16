package com.example.citigrow.data

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.ceil
import kotlin.math.roundToInt

data class CareerMatch(val percent: Int, val why: String, val gaps: List<String>)
data class Eligibility(val status: String, val reasons: List<String>)

object GrowRules {
    fun weeks(hours: Int, minutes: Int) = maxOf(1, ceil(hours * 60.0 / (maxOf(5, minutes) * 6)).toInt())
    fun match(p: JSONObject, c: JSONObject): CareerMatch? {
        val discovery = p.optJSONObject("discovery") ?: return null
        val traits = discovery.obj("traits")
        val weights = c.obj("w")
        val keys = weights.keys().asSequence().toList()
        val denominator = keys.sumOf { 3 * weights.optInt(it) }
        val numerator = keys.sumOf { minOf(3, traits.optInt(it)) * weights.optInt(it) }
        var score = 0.42 + 0.5 * if (denominator > 0) numerator.toDouble() / denominator else 0.0
        val gaps = mutableListOf<String>()
        p.optJSONObject("assessment")?.let { assessment ->
            c.obj("need").keys().forEach { skill ->
                val gap = c.obj("need").optInt(skill) - assessment.obj("levels").optInt(skill, 1)
                if (gap <= 0) score += 0.025 else { score -= 0.035 * gap; gaps.add(skill) }
            }
        }
        val constraints = discovery.obj("constraints")
        if (constraints.optBoolean("nearHome")) score += if (c.optBoolean("relocate")) -0.10 else 0.04
        if ((constraints.text("budget") == "limited" || p.text("budget") == "limited") && c.text("cost") == "high") score -= 0.07
        val experiences = discovery.arr("experiences").strings().intersect(c.arr("exp").strings().toSet())
        if (experiences.isNotEmpty()) score += 0.06
        val reasons = keys.filter { weights.optInt(it) >= 2 && traits.optInt(it) >= 2 }.map { "you enjoy $it work" }.toMutableList()
        if (experiences.isNotEmpty()) reasons.add("your everyday experience connects with this work")
        var why = "${c.text("title")} is suggested because ${reasons.joinToString(" and ").ifEmpty { "some of your interests match this work" }}."
        if (gaps.isNotEmpty()) why += " Build ${gaps.joinToString(", ")} with practice."
        if (constraints.optBoolean("nearHome") && c.optBoolean("relocate")) why += " This path usually means moving away from home."
        return CareerMatch((score * 100).roundToInt().coerceIn(30, 96), why, gaps)
    }
    fun eligibility(p: JSONObject, o: JSONObject): Eligibility {
        val education = when (p.text("edu")) { "iti" -> 11; "college" -> 13; else -> p.text("edu").toIntOrNull() ?: 0 }
        var status = "Appears eligible"
        val reasons = o.arr("rules").objects().map { r ->
            var outcome = 0 // yes, missing info, not met
            val reason = when (r.text("t")) {
                "cls" -> { if (education !in r.optInt("min")..r.optInt("max")) outcome = 2; "Education: level ${r.optInt("min")}–${r.optInt("max")} required; yours is $education." }
                "pass" -> { outcome = if (education > r.optInt("v")) 0 else if (education == r.optInt("v")) 1 else 2; "Class ${r.optInt("v")} pass required; confirm results on the official portal." }
                "age" -> { if (p.optInt("age") < r.optInt("min") || (r.optInt("max") > 0 && p.optInt("age") > r.optInt("max"))) outcome = 2; "Age ${r.optInt("min")}+${if (r.optInt("max") > 0) ", up to ${r.optInt("max")}" else ""}; you are ${p.optInt("age")}." }
                "state" -> { if (p.text("state") != r.text("v")) outcome = 2; "Residence required: ${r.text("v")}." }
                "income" -> { outcome = when (p.text("income")) { "below" -> 0; "above" -> 2; else -> 1 }; "Family income below ${r.text("v")}: ${p.text("income", "unknown")}." }
                "confirm" -> { val key = o.text("id") + ":" + r.text("id"); outcome = if (!p.obj("confirms").has(key)) 1 else if (p.obj("confirms").optBoolean(key)) 0 else 2; r.text("q") }
                else -> { outcome = 1; "Ask your teacher to verify this requirement." }
            }
            if (outcome == 2) status = "Does not meet a requirement"
            else if (outcome == 1 && status != "Does not meet a requirement") status = "More information needed"
            (when (outcome) { 0 -> "✓ "; 1 -> "? "; else -> "× " }) + reason
        }
        return Eligibility(status, reasons)
    }
    fun answer(store: GrowStore, p: JSONObject, question: String, answer: JSONObject) {
        p.obj("chatAns").put(question, answer)
        p.arr("chat").put(JSONObject().put("question", question).put("text", answer.text("text")))
        p.put("chatStep", p.optInt("chatStep") + 1)
        val traits = JSONObject(); val constraints = JSONObject().put("budget", p.text("budget", "unknown")); val experiences = JSONArray()
        p.obj("chatAns").keys().forEach { q ->
            val entry = p.obj("chatAns").getJSONObject(q)
            entry.obj("tags").keys().forEach { k -> traits.put(k, minOf(3, traits.optInt(k) + entry.obj("tags").optInt(k))) }
            if (entry.text("exp").isNotEmpty()) experiences.addUnique(entry.text("exp"))
            entry.obj("set").keys().forEach { k ->
                if (k == "study") p.put("study", entry.obj("set").optInt(k)) else constraints.put(k, entry.obj("set").get(k))
            }
        }
        if (p.optInt("chatStep") >= store.data.arr("FLOW").length()) p.put("discovery", JSONObject().put("traits", traits).put("experiences", experiences).put("constraints", constraints))
    }
    fun parse(store: GrowStore, question: String, text: String): JSONObject {
        val answer = JSONObject().put("text", text); val tags = answer.obj("tags"); val set = answer.obj("set")
        store.list("KW").forEach { k ->
            if (Regex(k.text("re"), RegexOption.IGNORE_CASE).containsMatchIn(text)) {
                k.obj("tags").keys().forEach { t -> tags.put(t, minOf(3, tags.optInt(t) + k.obj("tags").optInt(t))) }
                if (!answer.has("exp") && k.has("exp")) answer.put("exp", k.text("exp"))
            }
        }
        fun has(pattern: String) = Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(text)
        if (question == "where") { if (has("near|home|village|வீட்|ஊர்|घर|गांव|गाँव")) set.put("nearHome", true) else if (has("city|chennai|நகர|शहर")) set.put("nearHome", false) }
        if (question == "time") Regex("\\d+").find(text)?.value?.toIntOrNull()?.let { set.put("study", it.coerceIn(15, 60)) }
        if (question == "worry") { if (has("cost|money|fee|பணம்|செலவு|पैसा|फीस")) set.put("budget", "limited"); if (has("english|ஆங்கில|अंग्रेज")) set.put("english", true) }
        if (question == "pace") set.put("pace", if (has("own|self|சொந்த|अपनी")) "self" else "fixed")
        return answer
    }
    fun assess(items: List<JSONObject>, answers: List<Int>): JSONObject {
        val levels = JSONObject()
        items.map { it.text("skill") }.distinct().forEach { skill ->
            val indices = items.indices.filter { items[it].text("skill") == skill }
            val correct = indices.count { answers.getOrNull(it) == items[it].optInt("a") }
            levels.put(skill, if (correct == indices.size) 3 else if (correct > 0) 2 else 1)
        }
        return JSONObject().put("levels", levels)
    }
}
