package com.example.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.i18n.currentStrings
import com.example.domain.model.TwoFactorMethod
import com.example.domain.model.University
import com.example.presentation.ui.components.TwoFactorDialog
import com.example.presentation.viewmodel.AuthUiState
import com.example.ui.theme.NeptunBlue40
import com.example.ui.theme.NeptunCyan40

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
    onSelectLanguage: (com.example.domain.model.NeptunLanguage) -> Unit = {},
    onContinueOffline: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = currentStrings()
    var showUniversityDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Language Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = { showLanguageDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("login_language_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.currentLanguage.flagEmoji,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.currentLanguage.code.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = strings.languageSelectTitle,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Branding Crest & Header
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(NeptunBlue40, NeptunCyan40)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = strings.appName,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = strings.appName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = strings.loginBrandingSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.loginButton,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // University Selector Trigger
                    Text(
                        text = strings.institutionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showUniversityDialog = true }
                            .testTag("university_selector")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = strings.university,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val selectedUniName = uiState.selectedUniversity?.name ?: strings.selectUniversityPlaceholder
                                Text(
                                    text = selectedUniName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (uiState.selectedUniversity != null) {
                                    Text(
                                        text = "${uiState.selectedUniversity.city} • ${uiState.selectedUniversity.neptunUrl}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = strings.select,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Neptun Code Field
                    OutlinedTextField(
                        value = uiState.neptunCode,
                        onValueChange = onNeptunCodeChange,
                        label = { Text(strings.neptunCodeInputLabel) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = strings.neptunCode,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("neptun_code_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        label = { Text(strings.passwordInputLabel) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = strings.password,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) strings.close else strings.details
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onLoginClick()
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Error message
                    AnimatedVisibility(visible = uiState.errorMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Button
                    Button(
                        onClick = onLoginClick,
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button")
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = strings.loginButton,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Demo Credentials Fill (kizárólag debug változatban jelenik meg)
                    if (com.example.core.debug.DebugFeatures.isDemoAllowed) {
                        OutlinedButton(
                            onClick = onQuickDemoFill,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("quick_demo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = strings.quickDemoCredentials,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.quickDemoCredentials,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (uiState.isOfflineModeAvailable) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onContinueOffline,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("continue_offline_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = strings.offlineMode,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.offlineMode,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = strings.keystoreSecurityNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    // University Picker Dialog
    if (showUniversityDialog) {
        Dialog(onDismissRequest = { showUniversityDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.selectInstitutionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text(strings.searchUniversityPlaceholder) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = strings.search)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("university_search_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.filteredUniversities, key = { it.id }) { uni ->
                            val isSelected = uiState.selectedUniversity?.id == uni.id
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectUniversity(uni)
                                        showUniversityDialog = false
                                    }
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else Color.Transparent
                                    )
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = uni.shortName.take(3),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = uni.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${uni.city} • ${uni.neptunUrl}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        Dialog(onDismissRequest = { showLanguageDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = strings.languageSelectTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.universityLanguageInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    HorizontalDivider()

                    LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        items(uiState.supportedLanguages, key = { it.code }) { lang ->
                            val isSelected = lang.lcid == uiState.currentLanguage.lcid || lang.code.equals(uiState.currentLanguage.code, ignoreCase = true)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSelectLanguage(lang)
                                        showLanguageDialog = false
                                    }
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else Color.Transparent
                                    )
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = lang.flagEmoji,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lang.displayLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${lang.name} • LCID: ${lang.lcid}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = strings.select,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showLanguageDialog = false },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.close)
                    }
                }
            }
        }
    }

    // 2FA Dialog with Full Flow Support
    TwoFactorDialog(
        uiState = uiState,
        strings = strings,
        onTwoFactorCodeChange = onTwoFactorCodeChange,
        onTwoFactorMethodChange = onTwoFactorMethodChange,
        onRequestEmailCode = onRequestEmailCode,
        onSubmitTwoFactor = onSubmitTwoFactor,
        onCancelTwoFactor = onCancelTwoFactor
    )
}
