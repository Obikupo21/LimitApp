package com.limitapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limitapp.ui.LimitAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private var serviceEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LimitAppTheme {
                MainScreen(
                    serviceEnabled = serviceEnabled,
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    onTryPuzzle = { startActivity(PuzzleActivity.intent(this, packageName)) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // El usuario puede volver de Ajustes tras activar (o desactivar) el servicio.
        serviceEnabled = isBlockerServiceEnabled(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    serviceEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onTryPuzzle: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { BlockerPrefs(context) }

    var apps by remember { mutableStateOf<List<InstalledApp>?>(null) }
    var blocked by remember { mutableStateOf(prefs.blockedPackages) }
    var difficulty by remember { mutableStateOf(prefs.difficulty) }
    var unlockMinutes by remember { mutableStateOf(prefs.unlockMinutes) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.Default) { loadLaunchableApps(context) }
    }

    fun toggle(pkg: String, enabled: Boolean) {
        blocked = if (enabled) blocked + pkg else blocked - pkg
        prefs.blockedPackages = blocked
    }

    Scaffold(topBar = { TopAppBar(title = { Text("LimitApp") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
        ) {
            item { ServiceStatusCard(serviceEnabled, onOpenAccessibilitySettings) }

            item {
                SettingsCard(
                    difficulty = difficulty,
                    onDifficultyChange = { difficulty = it; prefs.difficulty = it },
                    unlockMinutes = unlockMinutes,
                    onUnlockMinutesChange = { unlockMinutes = it; prefs.unlockMinutes = it },
                    onTryPuzzle = onTryPuzzle,
                )
            }

            item {
                Text(
                    text = "Apps limitadas: ${blocked.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar app…") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }

            val loaded = apps
            if (loaded == null) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                // Las apps ya limitadas aparecen primero.
                val visible = loaded
                    .filter { query.isBlank() || it.label.contains(query.trim(), ignoreCase = true) }
                    .sortedByDescending { it.packageName in blocked }
                items(visible, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        checked = app.packageName in blocked,
                        onCheckedChange = { toggle(app.packageName, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceStatusCard(enabled: Boolean, onOpenSettings: () -> Unit) {
    val colors = if (enabled) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    }
    Card(colors = colors, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = if (enabled) "✓ Bloqueo activo" else "Bloqueo desactivado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            if (enabled) {
                Text("Las apps que marques abajo pedirán un acertijo al abrirse.")
            } else {
                Text(
                    "Para detectar cuándo abres una app limitada, activa el servicio " +
                        "\"LimitApp\" en Ajustes › Accesibilidad.",
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Si aparece como \"Ajuste restringido\": ve a Ajustes › Aplicaciones › " +
                            "LimitApp, pulsa ⋮ y elige \"Permitir ajustes restringidos\".",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(onClick = onOpenSettings) { Text("Abrir ajustes de accesibilidad") }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    difficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    unlockMinutes: Int,
    onUnlockMinutesChange: (Int) -> Unit,
    onTryPuzzle: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Dificultad del acertijo", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach {
                    FilterChip(
                        selected = it == difficulty,
                        onClick = { onDifficultyChange(it) },
                        label = { Text(it.label) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Tiempo de uso tras acertar", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BlockerPrefs.UNLOCK_OPTIONS.forEach {
                    FilterChip(
                        selected = it == unlockMinutes,
                        onClick = { onUnlockMinutesChange(it) },
                        label = { Text("$it min") },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onTryPuzzle) { Text("Probar un acertijo") }
        }
    }
}

@Composable
private fun AppRow(app: InstalledApp, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(40.dp))
        Spacer(Modifier.width(16.dp))
        Text(app.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
