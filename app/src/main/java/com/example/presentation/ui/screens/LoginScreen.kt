package com.example.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TwoFactorMethod
import com.example.domain.model.University
import com.example.presentation.ui.components.FilcChip
import com.example.presentation.ui.components.FilcFilterBar
import com.example.presentation.ui.components.FilcCard
import com.example.presentation.ui.components.FilcBottomSheet
import com.example.presentation.viewmodel.AuthUiState
import com.example.ui.theme.filcColors

/**
 * Bejelentkezés – a reFilc "v5 login" képével: halvány lime háttér, felül a márka,
 * középen a nagy, Montserrat címsor, alul pedig a 12 dp-es accent keretes
 * mezőkkel rendelkező bejelentkezési panel.
 */
@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectUniversity: (University) -> Unit,
    onNeptunCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onQuickDemoFill: () -> Unit,
    onTwoFactorCodeChange: (String) -> Unit = {},
    onTwoFactorMethodChange: (TwoFactorMethod) -> Unit = {},
    onRequestEmailCode: () -> Unit = {},
    onSubmitTwoFactor: () -> Unit = {},
    onCancelTwoFactor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    var showUniversitySheet by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(filc.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // --- márka sor -------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(filc.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.85f)
                    )
                }
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = "Neptun Mobile",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (filc.isLight) Color(0xFF394C0A) else filc.text
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "v" + com.example.BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted
                )
            }

            // --- hero szoveg ----------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 34.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "A Neptun,\nvégre normális kinézettel.",
                    style = MaterialTheme.typography.displaySmall,
                    color = if (filc.isLight) Color(0xFF394C0A) else filc.text,
                    lineHeight = 34.sp
                )
                Text(
                    text = "Órarend, jegyek, kreditkalkulátor, üzenetek és pénzügyek – egyetlen, gyors appban, ami nem 2004-ben készült.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = filc.text.copy(alpha = 0.7f),
                    lineHeight = 23.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FeatureChip("Élő órarend")
                    FeatureChip("Szellemjegy-kalki")
                    FeatureChip("Értesítések")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- bejelentkezasi panel --------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, filc.background)
                        )
                    )
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 14.dp)
                    .imePadding()
            ) {
                FilcCard(
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    if (uiState.isTwoFactorRequired) {
                        TwoFactorBlock(
                            uiState = uiState,
                            onTwoFactorCodeChange = onTwoFactorCodeChange,
                            onTwoFactorMethodChange = onTwoFactorMethodChange,
                            onRequestEmailCode = onRequestEmailCode,
                            onSubmitTwoFactor = {
                                keyboard?.hide()
                                onSubmitTwoFactor()
                            },
                            onCancelTwoFactor = onCancelTwoFactor
                        )
                    } else {
                        Text(
                            text = "Bejelentkezés",
                            style = MaterialTheme.typography.titleLarge,
                            color = filc.text
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        UniversityField(
                            university = uiState.selectedUniversity,
                            onClick = { showUniversitySheet = true },
                            modifier = Modifier.testTag("login_university_button")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LoginField(
                            value = uiState.neptunCode,
                            onValueChange = onNeptunCodeChange,
                            label = "Neptun kód",
                            placeholder = "pl. ABCDEF",
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next,
                            maxLength = 6,
                            modifier = Modifier.testTag("login_code_field")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LoginField(
                            value = uiState.password,
                            onValueChange = onPasswordChange,
                            label = "Jelszó",
                            placeholder = "••••••••",
                            isPassword = !passwordVisible,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible },
                            imeAction = ImeAction.Done,
                            onImeDone = {
                                keyboard?.hide()
                                onLoginClick()
                            },
                            modifier = Modifier.testTag("login_password_field")
                        )

                        AnimatedVisibility(
                            visible = uiState.errorMessage != null,
                            enter = fadeIn(tween(150)),
                            exit = fadeOut(tween(120))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(filc.red.copy(alpha = 0.12f))
                                    .padding(11.dp)
                            ) {
                                Text(
                                    text = uiState.errorMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = filc.red,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FilcLoginButton(
                            text = if (uiState.isLoading) "Bejelentkezés…" else "Belépés a Neptunba",
                            loading = uiState.isLoading,
                            onClick = {
                                keyboard?.hide()
                                onLoginClick()
                            },
                            modifier = Modifier.testTag("login_submit_button")
                        )

                        if (uiState.isOfflineModeAvailable) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(filc.text.copy(alpha = 0.045f))
                                    .clickable(onClick = onQuickDemoFill)
                                    .padding(vertical = 11.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = filc.yellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Demó adatok kitöltése (DEMO01)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = filc.textSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "kipróbálom",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = filc.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "A bejelentkezéssel a saját Neptun-hitelesítődet használod; az adatok csak ezen a telefonon tárolódnak.",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
        }

        UniversitySheet(
            visible = showUniversitySheet,
            uiState = uiState,
            onSearchQueryChange = onSearchQueryChange,
            onSelect = { university ->
                onSelectUniversity(university)
                showUniversitySheet = false
            },
            onDismiss = { showUniversitySheet = false }
        )
    }
}

@Composable
private fun FeatureChip(text: String) {
    val filc = filcColors()
    FilcChip(
        text = text,
        color = if (filc.isLight) Color(0xFF394C0A) else filc.text,
        background = filc.accent.copy(alpha = 0.28f),
        textSize = 12f
    )
}

/** A reFilc-féle 12 dp-es, accent keretes szövegmező. */
@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    onImeDone: () -> Unit = {},
    maxLength: Int = 0,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(if (maxLength > 0) input.take(maxLength) else input) },
        modifier = modifier
            .fillMaxWidth()
            .testTag("login_field_$label"),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = filc.textMuted
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        textStyle = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = filc.text
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (label == "Jelszó") {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onTogglePassword),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Elrejtés" else "Megjelenítés",
                        tint = filc.text.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Box(modifier = Modifier.size(40.dp))
            }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            imeAction = imeAction,
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(onDone = { onImeDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = filc.accent,
            unfocusedBorderColor = filc.accent.copy(alpha = 0.45f),
            focusedLabelColor = filc.accent,
            unfocusedLabelColor = filc.textMuted,
            cursorColor = filc.accent,
            focusedContainerColor = filc.surfaceRaised,
            unfocusedContainerColor = filc.surfaceRaised,
            disabledBorderColor = filc.hairline,
            errorBorderColor = filc.red
        )
    )
}

@Composable
private fun UniversityField(
    university: University?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(filc.surfaceRaised)
            .height(56.dp)
            .border(1.dp, filc.accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = filc.accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Egyetem",
                style = MaterialTheme.typography.labelSmall,
                color = filc.textMuted,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = university?.let { it.shortName.ifBlank { it.name } } ?: "Válassz egyetemet",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = filc.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "váltás",
            style = MaterialTheme.typography.labelSmall,
            color = filc.accent,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FilcLoginButton(
    text: String,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (loading) filc.accent.copy(alpha = 0.6f) else filc.accent)
            .clickable(enabled = !loading, onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = if (filc.isLight) Color.Black.copy(alpha = 0.7f) else Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(9.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (filc.isLight) Color(0xFF1C2605) else filc.onAccent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TwoFactorBlock(
    uiState: AuthUiState,
    onTwoFactorCodeChange: (String) -> Unit,
    onTwoFactorMethodChange: (TwoFactorMethod) -> Unit,
    onRequestEmailCode: () -> Unit,
    onSubmitTwoFactor: () -> Unit,
    onCancelTwoFactor: () -> Unit
) {
    val filc = filcColors()
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(filc.yellow.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = filc.yellow,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kétlépcsős azonosítás",
                    style = MaterialTheme.typography.titleLarge,
                    color = filc.text
                )
                Text(
                    text = "A Neptun rendszer megerősítést kér – írd be a kapott kódot.",
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        FilcFilterBar(
            options = listOf(TwoFactorMethod.EMAIL.displayName, TwoFactorMethod.TOTP.displayName),
            selectedIndex = if (uiState.twoFactorMethod == TwoFactorMethod.EMAIL) 0 else 1,
            onSelect = { index ->
                onTwoFactorMethodChange(
                    if (index == 0) TwoFactorMethod.EMAIL else TwoFactorMethod.TOTP
                )
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        LoginField(
            value = uiState.twoFactorCode,
            onValueChange = onTwoFactorCodeChange,
            label = "Ellenőrző kód",
            placeholder = if (uiState.codePrefix.isNotBlank()) "${uiState.codePrefix}-•••" else "6 karakter",
            imeAction = ImeAction.Done,
            onImeDone = onSubmitTwoFactor,
            maxLength = 6,
            modifier = Modifier.testTag("login_2fa_code_field")
        )

        AnimatedVisibility(visible = uiState.twoFactorSuccessMessage != null) {
            Text(
                text = uiState.twoFactorSuccessMessage.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = filc.green,
                modifier = Modifier.padding(top = 8.dp),
                lineHeight = 17.sp
            )
        }
        AnimatedVisibility(visible = uiState.twoFactorErrorMessage != null) {
            Text(
                text = uiState.twoFactorErrorMessage.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = filc.red,
                modifier = Modifier.padding(top = 8.dp),
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.twoFactorMethod == TwoFactorMethod.EMAIL) {
            FilcLoginButton(
                text = if (uiState.isEmailCodeRequested) "Új kód kérése" else "Kód kikérése e-mailbe",
                loading = uiState.isTwoFactorLoading,
                onClick = onRequestEmailCode,
                modifier = Modifier.testTag("login_2fa_request")
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(filc.text.copy(alpha = 0.06f))
                    .clickable(onClick = onCancelTwoFactor)
                    .padding(vertical = 13.dp)
                    .testTag("login_2fa_cancel"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mégse",
                    style = MaterialTheme.typography.titleSmall,
                    color = filc.text
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(filc.accent)
                    .clickable(onClick = onSubmitTwoFactor)
                    .padding(vertical = 13.dp)
                    .testTag("login_2fa_submit"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belépés",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (filc.isLight) Color(0xFF1C2605) else filc.onAccent
                )
            }
        }
    }
}

/** Egyetemválasztó lap – keresővel, a Filc "bottom card" formában. */
@Composable
private fun UniversitySheet(
    visible: Boolean,
    uiState: AuthUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelect: (University) -> Unit,
    onDismiss: () -> Unit
) {
    val filc = filcColors()
    val list = uiState.filteredUniversities.ifEmpty { uiState.universities }

    FilcBottomSheet(visible = visible, onDismiss = onDismiss, scrollable = false) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Válassz egyetemet",
                style = MaterialTheme.typography.titleLarge,
                color = filc.text,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(filc.text.copy(alpha = 0.06f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Bezárás",
                    tint = filc.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LoginField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            label = "Keresés",
            placeholder = "név, rövidítés vagy város",
            modifier = Modifier.testTag("login_university_search")
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp)
        ) {
            items(list, key = { it.id }) { university ->
                val selected = uiState.selectedUniversity?.id == university.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) filc.accent.copy(alpha = 0.16f) else Color.Transparent
                        )
                        .clickable(onClick = { onSelect(university) })
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                com.example.ui.theme.stringToAvatarColor(university.shortName.ifBlank { university.name })
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = university.shortName.take(2).uppercase(),
                            color = Color.Black.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(11.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = university.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = filc.text,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${university.city} · ${university.neptunUrl}",
                            style = MaterialTheme.typography.labelSmall,
                            color = filc.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (selected) {
                        FilcChip(text = "kiválasztva", color = filc.accent)
                    }
                }
            }
        }
    }
}
