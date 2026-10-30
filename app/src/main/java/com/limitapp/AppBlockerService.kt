package com.limitapp

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Vigila qué app pasa a primer plano. Si es una app limitada y no está desbloqueada,
 * muestra encima la pantalla del acertijo.
 */
class AppBlockerService : AccessibilityService() {

    private val prefs by lazy { BlockerPrefs(this) }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg !in prefs.blockedPackages || prefs.isUnlocked(pkg)) return

        // PuzzleActivity es singleTask: si ya está abierta, se reutiliza en lugar de duplicarse.
        startActivity(PuzzleActivity.intent(this, pkg))
    }

    override fun onInterrupt() = Unit
}
