package com.example.citigrow.voice

import org.json.JSONObject
import java.util.Locale

/** Mirrors the cached IndicVitsTokenizer: retain known characters, lowercase unknown
 * characters, drop out-of-vocabulary characters, strip, then intersperse blank id 0.
 * No phonemizer and no generic BERT/WordPiece tokenizer belong in this pipeline. */
class VitsTokenizer(vocab: JSONObject) {
    private val tokens = vocab.keys().asSequence().associateWith { vocab.getLong(it) }
    fun encode(text: String): LongArray {
        val normalized = buildString {
            text.codePoints().forEach { point ->
                val character = String(Character.toChars(point))
                append(if (tokens.containsKey(character)) character else character.lowercase(Locale.ROOT))
            }
        }
        val cleaned = buildString {
            normalized.codePoints().forEach { point ->
                val character = String(Character.toChars(point))
                if (tokens.containsKey(character)) append(character)
            }
        }.trim { Character.isWhitespace(it) || Character.isSpaceChar(it) }
        val ids = cleaned.codePoints().toArray().map { tokens.getValue(String(Character.toChars(it))) }
        require(ids.isNotEmpty()) { "This text has no supported speech characters." }
        require(ids.size <= 500) { "Split long speech into shorter sentences." }
        return LongArray(ids.size * 2 + 1).also { out -> ids.forEachIndexed { i, value -> out[i*2+1] = value } }
    }
}
