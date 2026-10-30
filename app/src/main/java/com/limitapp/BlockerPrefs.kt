package com.limitapp

import android.content.Context
import androidx.core.content.edit

/** Configuración persistente: qué apps están limitadas, dificultad y tiempo de desbloqueo. */
class BlockerPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("limitapp_prefs", Context.MODE_PRIVATE)

    var blockedPackages: Set<String>
        get() = prefs.getStringSet(KEY_BLOCKED, emptySet())!!.toSet()
        set(value) = prefs.edit { putStringSet(KEY_BLOCKED, value) }

    var difficulty: Difficulty
        get() = Difficulty.entries.firstOrNull { it.name == prefs.getString(KEY_DIFFICULTY, null) }
            ?: Difficulty.MEDIO
        set(value) = prefs.edit { putString(KEY_DIFFICULTY, value.name) }

    /** Minutos que una app permanece desbloqueada tras resolver el acertijo. */
    var unlockMinutes: Int
        get() = prefs.getInt(KEY_UNLOCK_MINUTES, 5)
        set(value) = prefs.edit { putInt(KEY_UNLOCK_MINUTES, value) }

    fun isUnlocked(packageName: String): Boolean =
        System.currentTimeMillis() < prefs.getLong(unlockKey(packageName), 0L)

    fun unlock(packageName: String) = prefs.edit {
        putLong(unlockKey(packageName), System.currentTimeMillis() + unlockMinutes * 60_000L)
    }

    private fun unlockKey(packageName: String) = "unlocked_until_$packageName"

    companion object {
        private const val KEY_BLOCKED = "blocked_packages"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_UNLOCK_MINUTES = "unlock_minutes"

        val UNLOCK_OPTIONS = listOf(1, 5, 15, 30)
    }
}
