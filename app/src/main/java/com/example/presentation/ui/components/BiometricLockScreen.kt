package com.example.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.core.security.BiometricLockHelper

/**
 * Teljes képernyős zár: biometrikus hitelesítést kér az app tartalmának feloldásához.
 */
@Composable
fun BiometricLockScreen(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var message by remember { mutableStateOf<String?>(null) }
    var biometricAvailable by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        if (activity != null) {
            val available = BiometricLockHelper.canAuthenticate(activity)
            biometricAvailable = available
            if (available) {
                BiometricLockHelper.showPrompt(
                    activity = activity,
                    onSuccess = onUnlock,
                    onError = { message = it }
                )
            }
        } else {
            biometricAvailable = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Zárolva",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Az alkalmazás zárolva van",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    biometricAvailable == false -> "A biometrikus hitelesítés nem elérhető ezen az eszközön."
                    else -> "Erősítsd meg az azonosságod az ujjlenyomatoddal vagy arcoddal."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            message?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (activity != null && biometricAvailable == true) {
                        BiometricLockHelper.showPrompt(
                            activity = activity,
                            onSuccess = onUnlock,
                            onError = { message = it }
                        )
                    } else {
                        onUnlock()
                    }
                },
                enabled = biometricAvailable != null
            ) {
                Text("Feloldás")
            }

            if (biometricAvailable == false) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onUnlock) {
                    Text("Később")
                }
            }
        }
    }
}
