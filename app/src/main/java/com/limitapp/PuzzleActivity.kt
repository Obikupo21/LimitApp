package com.limitapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import com.limitapp.ui.LimitAppTheme

/** Pantalla que se superpone a una app limitada hasta que el usuario resuelve el acertijo. */
class PuzzleActivity : ComponentActivity() {

    private var targetPackage by mutableStateOf("")
    private var solved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        targetPackage = intent.getStringExtra(EXTRA_PACKAGE) ?: run { finish(); return }

        // "Atrás" no devuelve a la app bloqueada: lleva a la pantalla de inicio.
        onBackPressedDispatcher.addCallback(this) { goHome() }

        setContent {
            LimitAppTheme {
                PuzzleScreen(
                    targetPackage = targetPackage,
                    onSolved = {
                        solved = true
                        BlockerPrefs(this).unlock(targetPackage)
                        finish()
                    },
                    onGiveUp = ::goHome,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(EXTRA_PACKAGE)?.let { targetPackage = it }
    }

    override fun onStop() {
        super.onStop()
        // Si el usuario sale sin resolverlo, se descarta: la próxima vez habrá un acertijo nuevo.
        if (!solved && !isChangingConfigurations) finish()
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        finish()
    }

    companion object {
        private const val EXTRA_PACKAGE = "target_package"

        fun intent(context: Context, packageName: String): Intent =
            Intent(context, PuzzleActivity::class.java)
                .putExtra(EXTRA_PACKAGE, packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
}

@Composable
private fun PuzzleScreen(
    targetPackage: String,
    onSolved: () -> Unit,
    onGiveUp: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { BlockerPrefs(context) }
    val appLabel = remember(targetPackage) { loadAppLabel(context, targetPackage) }
    val appIcon = remember(targetPackage) { loadAppIcon(context, targetPackage) }

    var puzzle by remember(targetPackage) { mutableStateOf(PuzzleGenerator.generate(prefs.difficulty)) }
    var input by remember(puzzle) { mutableStateOf("") }
    var failures by remember(targetPackage) { mutableIntStateOf(0) }
    var error by remember(targetPackage) { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    fun check() {
        val value = input.trim().toIntOrNull() ?: run {
            error = "Introduce un número"
            return
        }
        if (value == puzzle.answer) {
            onSolved()
        } else {
            failures++
            error = "Incorrecto. Prueba con este otro."
            puzzle = PuzzleGenerator.generate(prefs.difficulty)
        }
    }

    LaunchedEffect(puzzle) { runCatching { focusRequester.requestFocus() } }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            appIcon?.let {
                Image(bitmap = it, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
            }
            Text(
                text = "$appLabel está limitada",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Resuelve el acertijo para entrar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = puzzle.question,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    fontSize = 32.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = input,
                onValueChange = { new ->
                    input = new.filter { it.isDigit() }.take(6)
                    error = null
                },
                label = { Text("Tu respuesta") },
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { check() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            Spacer(Modifier.height(16.dp))

            Button(onClick = ::check, modifier = Modifier.fillMaxWidth()) {
                Text("Desbloquear ${prefs.unlockMinutes} min")
            }
            TextButton(onClick = onGiveUp) {
                Text("Mejor no, volver al inicio")
            }
            if (failures > 0) {
                Text(
                    text = "Intentos fallidos: $failures",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
