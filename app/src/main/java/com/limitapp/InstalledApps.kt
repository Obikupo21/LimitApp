package com.limitapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class InstalledApp(val packageName: String, val label: String, val icon: ImageBitmap)

private const val ICON_SIZE_PX = 96

/** Apps con icono en el lanzador, ordenadas alfabéticamente (sin incluir esta misma app). */
fun loadLaunchableApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(launcherIntent, 0)
        .distinctBy { it.activityInfo.packageName }
        .filter { it.activityInfo.packageName != context.packageName }
        .map {
            InstalledApp(
                packageName = it.activityInfo.packageName,
                label = it.loadLabel(pm).toString(),
                icon = it.loadIcon(pm).toBitmap(ICON_SIZE_PX, ICON_SIZE_PX).asImageBitmap(),
            )
        }
        .sortedBy { it.label.lowercase() }
}

fun loadAppLabel(context: Context, packageName: String): String = runCatching {
    val pm = context.packageManager
    pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString()
}.getOrDefault(packageName)

fun loadAppIcon(context: Context, packageName: String): ImageBitmap? = runCatching {
    context.packageManager.getApplicationIcon(packageName)
        .toBitmap(ICON_SIZE_PX, ICON_SIZE_PX).asImageBitmap()
}.getOrNull()

fun isBlockerServiceEnabled(context: Context): Boolean {
    val expected = ComponentName(context, AppBlockerService::class.java)
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == expected }
}
