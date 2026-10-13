package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String,
    val flag: String
)

val SupportedCurrencies = listOf(
    CurrencyOption("USD", "$", "US Dollar", "🇺🇸"),
    CurrencyOption("PHP", "₱", "Philippine Peso", "🇵🇭"),
    CurrencyOption("EUR", "€", "Euro", "🇪🇺"),
    CurrencyOption("GBP", "£", "British Pound", "🇬🇧"),
    CurrencyOption("JPY", "¥", "Japanese Yen", "🇯🇵"),
    CurrencyOption("CAD", "CA$", "Canadian Dollar", "🇨🇦"),
    CurrencyOption("AUD", "AU$", "Australian Dollar", "🇦🇺"),
    CurrencyOption("INR", "₹", "Indian Rupee", "🇮🇳")
)

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("budgetwise_prefs", Context.MODE_PRIVATE)

    fun getThemeMode(): AppThemeMode {
        val modeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return try {
            AppThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun getCurrency(): CurrencyOption {
        val code = prefs.getString(KEY_CURRENCY_CODE, "USD") ?: "USD"
        return SupportedCurrencies.find { it.code == code } ?: SupportedCurrencies.first()
    }

    fun setCurrency(currency: CurrencyOption) {
        prefs.edit().putString(KEY_CURRENCY_CODE, currency.code).apply()
    }

    fun isMorningReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_MORNING_ENABLED, true)
    }

    fun setMorningReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MORNING_ENABLED, enabled).apply()
    }

    fun getMorningHour(): Int = prefs.getInt(KEY_MORNING_HOUR, 8) // 8:00 AM
    fun getMorningMinute(): Int = prefs.getInt(KEY_MORNING_MINUTE, 30)

    fun setMorningTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_MORNING_HOUR, hour)
            .putInt(KEY_MORNING_MINUTE, minute)
            .apply()
    }

    fun isEveningReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_EVENING_ENABLED, true)
    }

    fun setEveningReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EVENING_ENABLED, enabled).apply()
    }

    fun getEveningHour(): Int = prefs.getInt(KEY_EVENING_HOUR, 20) // 8:00 PM
    fun getEveningMinute(): Int = prefs.getInt(KEY_EVENING_MINUTE, 0)

    fun setEveningTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_EVENING_HOUR, hour)
            .putInt(KEY_EVENING_MINUTE, minute)
            .apply()
    }

    companion object {
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val KEY_CURRENCY_CODE = "currency_code"
        private const val KEY_MORNING_ENABLED = "morning_reminder_enabled"
        private const val KEY_MORNING_HOUR = "morning_reminder_hour"
        private const val KEY_MORNING_MINUTE = "morning_reminder_minute"
        private const val KEY_EVENING_ENABLED = "evening_reminder_enabled"
        private const val KEY_EVENING_HOUR = "evening_reminder_hour"
        private const val KEY_EVENING_MINUTE = "evening_reminder_minute"
    }
}
