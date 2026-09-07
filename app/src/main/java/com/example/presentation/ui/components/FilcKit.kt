package com.example.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.filcColors
import com.example.ui.theme.stringToAvatarColor

/* ------------------------------------------------------------------------- *
 *  Filc UI-készlet: panelek, gombok, pilulák, lapok – a reFilc
 *  `refilc_mobile_ui/common/` mappájának Compose megfelelői.
 * ------------------------------------------------------------------------- */

/**
 * Filc kártya-háttér: soft, mélyre húzott árnyék (világosban), halvány lime
 * felület. A reFilc `Panel` dobozánál `BoxShadow(offset(0, 21), blur 23)`.
 */
@Composable
fun Modifier.filcCard(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 16.dp,
    background: Color = filcColors().surface
): Modifier {
    val filc = filcColors()
    return this
        .shadow(
            elevation = if (filc.isLight) elevation else 0.dp,
            shape = shape,
            clip = false,
            ambientColor = filc.shadow.copy(alpha = 0.30f),
            spotColor = filc.shadow.copy(alpha = 0.50f)
        )
        .clip(shape)
        .background(background)
}

/** Panel: opcionális cím + kártyatest. A cím a reFilc `PanelTitle` mintájára készült. */
@Composable
fun FilcPanel(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleTrailing: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(10.dp),
    shape: Shape = RoundedCornerShape(16.dp),
    background: Color = filcColors().surface,
    content: @Composable ColumnScope.() -> Unit
) {
    val filc = filcColors()
    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null || titleTrailing != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = filc.text.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (titleTrailing != null) titleTrailing()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .filcCard(shape = shape, background = background)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

/** Cím nélküli, sima kártya. */
@Composable
fun FilcCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(14.dp),
    background: Color = filcColors().surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .filcCard(shape = shape, background = background)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content
    )
}

/**
 * `PanelButton`: panelen belüli, 12 dp sarkú sor ikon + cím + érték / nyíl.
 * A `filled = true` a halvány, áttetsző kitöltés (reFilc:
 * `Colors.white.withValues(alpha = .35)`).
 */
@Composable
fun FilcPanelButton(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    filled: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val filc = filcColors()
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (filled) {
                    Modifier.background(Color.White.copy(alpha = if (filc.isLight) 0.35f else 0.05f))
                } else {
                    Modifier
                }
            )
            .then(if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Box(modifier = Modifier.padding(end = 12.dp), content = leading)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) filc.text else filc.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = filc.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(content = trailing)
        }
    }
}

/** Kis, pilula címke (pl. típus, kredit, státusz). */
@Composable
fun FilcChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = filcColors().accent,
    background: Color = color.copy(alpha = 0.18f),
    textSize: Float = 11f
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(45.dp))
            .background(background)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = textSize.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Kis, kerek ikon gomb (frissítés, vissza, küldés) – a Filc fejlécekben.
 */
@Composable
fun FilcIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = filcColors().icon,
    size: Dp = 38.dp
) {
    val filc = filcColors()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(filc.text.copy(alpha = 0.05f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(size / 2)
        )
    }
}

/** Kör alakú, keretes ikon – a reFilc `RoundBorderIcon`. */
@Composable
fun RoundBorderIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = filcColors().accent.copy(alpha = 0.75f),
    iconSize: Dp = 16.dp,
    strokeWidth: Dp = 1.5.dp,
    paddingValue: Dp = 6.dp,
    background: Color = Color.Transparent
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .border(BorderStroke(strokeWidth, color), CircleShape)
            .padding(paddingValue),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Színes pont – a reFilc `Dot`. */
@Composable
fun FilcDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

/** Vékony, pilula alakú haladássáv. */
@Composable
fun FilcProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = filcColors().accent,
    trackColor: Color = filcColors().text.copy(alpha = 0.08f),
    barHeight: Dp = 8.dp
) {
    val clamped = if (progress.isNaN()) 0f else progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(RoundedCornerShape(45.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(barHeight)
                .clip(RoundedCornerShape(45.dp))
                .background(color)
        )
    }
}

/**
 * Pilula tab-sor (`FilterBar`): a kiválasztott elem mögött 20%-os accent, a
 * szöveg accent színű, a többi halvány.
 */
@Composable
fun FilcFilterBar(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = filcColors().accent,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp)
) {
    val filc = filcColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(45.dp))
                    .background(if (selected) accentColor.copy(alpha = 0.22f) else Color.Transparent)
                    .clickable(onClick = { onSelect(index) })
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) accentColor else filc.text.copy(alpha = 0.65f),
                    maxLines = 1
                )
            }
        }
    }
}

/** Üres állapot – a reFilc `Empty` widget. */
@Composable
fun FilcEmptyState(
    icon: ImageVector,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    val filc = filcColors()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(filc.text.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = filc.textMuted,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = filc.text.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = filc.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
        if (action != null) {
            Spacer(modifier = Modifier.height(6.dp))
            action()
        }
    }
}

/**
 * Lebegő alsó kártya (`bottom_card.dart`): scrim + 12 dp margin + 18 dp
 * sarok + 42×4-es fogantyú. A tartalom görgethető, a kártya pedig a rendszer
 * sávok fölött marad.
 */
@Composable
fun FilcBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val filc = filcColors()
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(160)) + slideInVertically(tween(260)) { it / 2 },
        exit = fadeOut(tween(120)) + slideOutVertically(tween(200)) { it / 2 },
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(filc.scrim)
                .clickable(onClick = onDismiss)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .clip(RoundedCornerShape(18.dp))
                    .background(filc.surface)
                    .clickable(onClick = {})
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(45.dp))
                            .background(filc.text.copy(alpha = 0.10f))
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 10.dp)
                        .then(
                            if (scrollable) {
                                Modifier.verticalScroll(rememberScrollState())
                            } else {
                                Modifier
                            }
                        ),
                    content = content
                )
            }
        }
    }
}

/** Kezdőbetűs avatár – a reFilc `ProfileImage` helyettesítője. */
@Composable
fun FilcAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showBadge: Boolean = false,
    badgeColor: Color = filcColors().red,
    background: Color? = null
) {
    val initials = name.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .let { parts ->
            when {
                parts.size >= 2 -> "${parts[0].firstOrNull() ?: '?'}${parts[1].firstOrNull() ?: '?'}"
                parts.size == 1 -> parts[0].take(2)
                else -> "?"
            }
        }
        .uppercase()
    val bg = background ?: stringToAvatarColor(name)

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.Black.copy(alpha = 0.85f),
                fontSize = (size.value * 0.34f).sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size((size * 0.28f).coerceAtLeast(8.dp))
                    .clip(CircleShape)
                    .background(badgeColor)
            )
        }
    }
}

/** Kapcsoló – a reFilc `CustomSwitch` mintájára, Material3 helyett teljesen saját. */
@Composable
fun FilcSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val filc = filcColors()
    val trackColor by animateColorAsState(
        targetValue = if (checked) filc.accent else filc.text.copy(alpha = 0.12f),
        animationSpec = tween(180),
        label = "switch_track"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 21.dp else 3.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "switch_thumb"
    )

    Box(
        modifier = modifier
            .size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(45.dp))
            .background(trackColor)
            .then(if (enabled) Modifier.clickable(onClick = { onCheckedChange(!checked) }) else Modifier)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = 3.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/** Elválasztó vonal. */
@Composable
fun FilcDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = filcColors().hairline
) {
    Box(modifier = modifier.fillMaxWidth().height(thickness).background(color))
}

/** Kis felirat / cím. */
@Composable
fun FilcOverline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = filcColors().textMuted
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = color
    )
}

/** Statisztika-mutató: cím + nagy érték + opcionális keret. */
@Composable
fun FilcStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = filcColors().text,
    caption: String? = null,
    borderColor: Color? = null
) {
    val filc = filcColors()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (borderColor != null) {
                    Modifier.border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = filc.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            maxLines = 1
        )
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = filc.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
