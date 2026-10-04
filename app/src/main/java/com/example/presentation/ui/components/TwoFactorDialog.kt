package com.example.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.i18n.AppStrings
import com.example.domain.model.TwoFactorMethod
import com.example.presentation.viewmodel.AuthUiState

@Composable
fun TwoFactorDialog(
    uiState: AuthUiState,
    strings: AppStrings,
    onTwoFactorCodeChange: (String) -> Unit,
    onTwoFactorMethodChange: (TwoFactorMethod) -> Unit,
    onRequestEmailCode: () -> Unit,
    onSubmitTwoFactor: () -> Unit,
    onCancelTwoFactor: () -> Unit
) {
    if (!uiState.isTwoFactorRequired) return

    Dialog(onDismissRequest = onCancelTwoFactor) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = strings.twoFactorHeader,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = strings.twoFactorHeader,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = strings.twoFactorAccountPrompt(uiState.neptunCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Method Selector Tabs
                TabRow(
                    selectedTabIndex = if (uiState.twoFactorMethod == TwoFactorMethod.EMAIL) 0 else 1,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = uiState.twoFactorMethod == TwoFactorMethod.EMAIL,
                        onClick = { onTwoFactorMethodChange(TwoFactorMethod.EMAIL) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.twoFactorEmailTab, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = uiState.twoFactorMethod == TwoFactorMethod.TOTP,
                        onClick = { onTwoFactorMethodChange(TwoFactorMethod.TOTP) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.twoFactorTotpTab, fontSize = 13.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content: Email Flow
                if (uiState.twoFactorMethod == TwoFactorMethod.EMAIL) {
                    if (!uiState.isEmailCodeRequested) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = strings.twoFactorEmailPrompt,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (uiState.twoFactorSession?.hasTotp == false) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = strings.twoFactorNoTotpNote,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onRequestEmailCode,
                            enabled = !uiState.isTwoFactorLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("request_email_code_button")
                        ) {
                            if (uiState.isTwoFactorLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.twoFactorRequestingCode)
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.twoFactorRequestEmailBtn)
                            }
                        }
                    } else {
                        // Email code is requested, show prefix and input
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = strings.twoFactorCodeSent,
                                        color = Color(0xFF15803D),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (uiState.codePrefix.isNotEmpty()) {
                                        Text(
                                            text = "${strings.twoFactorCodePrefix} ${uiState.codePrefix}-",
                                            color = Color(0xFF166534),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.codePrefix.isNotEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "${uiState.codePrefix}-",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = uiState.twoFactorCode,
                                onValueChange = onTwoFactorCodeChange,
                                label = { Text(strings.twoFactorEmailCodeInputLabel) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("two_factor_code_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val showTestCodeBtn = com.example.BuildConfig.DEBUG && com.example.core.debug.DebugFeatures.isDemoAllowed
                        if (showTestCodeBtn) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onRequestEmailCode,
                                    enabled = !uiState.isTwoFactorLoading,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(strings.twoFactorResendBtn, fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { onTwoFactorCodeChange("999999") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(strings.twoFactorTestCodeBtn, fontSize = 11.sp)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = onRequestEmailCode,
                                enabled = !uiState.isTwoFactorLoading,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Text(strings.twoFactorResendBtn, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // TOTP Flow
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = strings.twoFactorTotpPrompt,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (uiState.twoFactorSession?.hasTotp == false) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strings.twoFactorNoTotpNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = uiState.twoFactorCode,
                        onValueChange = onTwoFactorCodeChange,
                        label = { Text(strings.twoFactorTotpInputLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("two_factor_code_input")
                    )
                }

                // 2FA Error message
                AnimatedVisibility(visible = uiState.twoFactorErrorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Text(
                            text = uiState.twoFactorErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelTwoFactor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(strings.cancel)
                    }
                    Button(
                        onClick = onSubmitTwoFactor,
                        enabled = uiState.twoFactorCode.length >= 4 && !uiState.isTwoFactorLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isTwoFactorLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(strings.twoFactorVerifyBtn)
                        }
                    }
                }
            }
        }
    }
}
