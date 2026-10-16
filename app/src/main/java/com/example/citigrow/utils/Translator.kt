package com.example.citigrow.utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Small, Compose-aware translation store used by the language landing screen.
 *
 * English text is used as the lookup key and as the fallback when a translation
 * has not been added yet. The selected language is retained across app launches.
 */
object Translator {
    const val ENGLISH = "en"
    const val HINDI = "hi"
    const val KANNADA = "kn"
    const val TAMIL = "ta"

    private const val PREFS_NAME = "citigrow_preferences"
    private const val LANGUAGE_KEY = "selected_language"

    var currentLanguage by mutableStateOf(ENGLISH)
        private set

    fun loadLanguage(context: Context) {
        val savedLanguage = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, ENGLISH)

        currentLanguage = savedLanguage
            ?.takeIf(::isSupported)
            ?: ENGLISH
    }

    fun setLanguage(context: Context, languageCode: String) {
        if (!isSupported(languageCode)) return

        currentLanguage = languageCode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_KEY, languageCode)
            .apply()
    }

    fun translate(englishText: String): String {
        if (currentLanguage == ENGLISH) return englishText
        return translations[currentLanguage]?.get(englishText) ?: englishText
    }

    private fun isSupported(languageCode: String): Boolean =
        languageCode == ENGLISH || translations.containsKey(languageCode)

    private val translations = mapOf(
        HINDI to mapOf(
            "Grow with your city" to "अपने शहर के साथ आगे बढ़ें",
            "Choose your language" to "अपनी भाषा चुनें",
            "Continue" to "जारी रखें",
            "Welcome to CitiGrow" to "सिटीग्रो में आपका स्वागत है",
            "Your selected language is Hindi." to "आपकी चुनी हुई भाषा हिंदी है।",
        ),
        KANNADA to mapOf(
            "Grow with your city" to "ನಿಮ್ಮ ನಗರದೊಂದಿಗೆ ಬೆಳೆಯಿರಿ",
            "Choose your language" to "ನಿಮ್ಮ ಭಾಷೆಯನ್ನು ಆಯ್ಕೆಮಾಡಿ",
            "Continue" to "ಮುಂದುವರಿಸಿ",
            "Welcome to CitiGrow" to "ಸಿಟಿಗ್ರೋಗೆ ಸುಸ್ವಾಗತ",
            "Your selected language is Kannada." to "ನೀವು ಆಯ್ಕೆ ಮಾಡಿದ ಭಾಷೆ ಕನ್ನಡ.",
        ),
        TAMIL to mapOf(
            "Grow with your city" to "உங்கள் நகரத்துடன் வளருங்கள்",
            "Choose your language" to "உங்கள் மொழியைத் தேர்ந்தெடுக்கவும்",
            "Continue" to "தொடரவும்",
            "Welcome to CitiGrow" to "சிட்டிக்ரோவிற்கு வரவேற்கிறோம்",
            "Your selected language is Tamil." to "நீங்கள் தேர்ந்தெடுத்த மொழி தமிழ்.",
        ),
    )
}

fun tr(englishText: String): String = Translator.translate(englishText)
