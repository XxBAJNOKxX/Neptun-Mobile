package com.example.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CourseType
import com.example.domain.model.FinanceStatus
import com.example.ui.theme.filcColors
import kotlin.math.abs

/** Magyar tizedesjelölés: 4.25 -> "4,25" (a reFilc is így cserél). */
fun formatHungarian(value: Double, decimals: Int = 2): String {
    val pattern = "%." + decimals + "f"
    return String.format(java.util.Locale.US, pattern, value).replace('.', ',')
}

/**
 * Jegyjelzés: kör, a reFilc `GradeValueWidget(fill: true)` mintájára. A
 * `filled` valódi, kitöltött kör, egyébként halvány kitöltésű változat.
 */
@Composable
fun GradeBadge(
    grade: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    filled: Boolean = false,
    isGhost: Boolean = false,
    text: String? = null
) {
    val filc = filcColors()
    val baseColor = if (isGhost) filc.ghost else filc.gradeColor(grade)
    val noGrade = grade == null && text == null

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (filled) baseColor else baseColor.copy(alpha = if (noGrade) 0.10f else 0.20f)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (noGrade) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Nincs jegy",
                tint = filc.textMuted,
                modifier = Modifier.size(size * 0.42f)
            )
        } else {
            val bright = baseColor == filc.gradeThree || baseColor == filc.gradeFour
            Text(
                text = text ?: grade.toString(),
                color = when {
                    filled && bright -> Color.Black.copy(alpha = 0.85f)
                    filled -> Color.White
                    else -> baseColor
                },
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = (size.value * 0.44f).sp
            )
        }
    }
}

/**
 * Átlag-mutató (`AverageDisplay`): 56×28 dp pilula, színezett háttérrel;
 * `dashed = true` esetén szaggatott keret – a szellemjegyes jövendő átlag.
 */
@Composable
fun AveragePill(
    average: Double,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    dashed: Boolean = false,
    bordered: Boolean = false,
    label: String? = null
) {
    val filc = filcColors()
    val empty = average <= 0.0
    val color = if (empty) filc.text.copy(alpha = 0.8f) else filc.averageColor(average)
    val shape = RoundedCornerShape(45.dp * scale)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 56.dp * scale, height = 28.dp * scale)
                .clip(shape)
                .then(
                    when {
                        bordered && dashed -> Modifier.drawBehind {
                            val inset = 1.2.dp.toPx()
                            drawRoundRect(
                                color = color.copy(alpha = 0.55f),
                                topLeft = Offset(inset, inset),
                                size = Size(size.width - inset * 2, size.height - inset * 2),
                                cornerRadius = CornerRadius(45.dp.toPx(), 45.dp.toPx()),
                                style = Stroke(width = 1.4.dp.toPx()),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(6.dp.toPx(), 5.dp.toPx()),
                                    0f
                                )
                            )
                        }

                        bordered -> Modifier.drawBehind {
                            drawRoundRect(
                                color = color.copy(alpha = 0.5f),
                                cornerRadius = CornerRadius(45.dp.toPx(), 45.dp.toPx()),
                                style = Stroke(width = 1.4.dp.toPx())
                            )
                        }

                        else -> Modifier.background(color.copy(alpha = if (empty) 0.15f else 0.25f))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (empty) "-" else formatHungarian(average),
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp * scale,
                maxLines = 1
            )
        }
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = filc.textMuted,
                fontSize = 10.sp * scale,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

/** Trend-mutató (`trend_display.dart`): előre zöld, hátra piros. */
@Composable
fun TrendDisplay(
    delta: Double,
    modifier: Modifier = Modifier,
    suffix: String = ""
) {
    val filc = filcColors()
    if (abs(delta) < 0.005) {
        Text(
            text = "±0" + suffix,
            modifier = modifier,
            style = MaterialTheme.typography.labelMedium,
            color = filc.textMuted
        )
        return
    }
    val positive = delta > 0
    val color = if (positive) filc.green else filc.red
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(45.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (positive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = (if (positive) "+" else "-") + formatHungarian(abs(delta)) + suffix,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Oktatás típusát jelző címke (EA / GY / LB / SZ / VH). */
@Composable
fun CourseTypeBadge(
    courseType: CourseType,
    modifier: Modifier = Modifier,
    showName: Boolean = false
) {
    val filc = filcColors()
    val color = when (courseType) {
        CourseType.LECTURE -> filc.blue
        CourseType.PRACTICE -> filc.green
        CourseType.LAB -> filc.purple
        CourseType.SEMINAR -> filc.yellow
        CourseType.EXAM -> filc.red
    }
    val shortLabel = when (courseType) {
        CourseType.LECTURE -> "EA"
        CourseType.PRACTICE -> "GY"
        CourseType.LAB -> "LB"
        CourseType.SEMINAR -> "SZ"
        CourseType.EXAM -> "VH"
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (showName) courseType.displayName else shortLabel,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
    }
}

/** Pénzügyi tétel állapotjelzője. */
@Composable
fun FinanceStatusBadge(
    status: FinanceStatus,
    modifier: Modifier = Modifier
) {
    val filc = filcColors()
    val color = when (status) {
        FinanceStatus.COMPLETED -> filc.green
        FinanceStatus.PENDING -> filc.orange
        FinanceStatus.OVERDUE -> filc.red
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(45.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Olvasatlan jelző: kis karika a sor szélén. */
@Composable
fun UnreadDot(
    modifier: Modifier = Modifier,
    color: Color = filcColors().accent,
    diameter: Dp = 9.dp
) {
    Box(
        modifier = modifier
            .size(diameter)
            .clip(CircleShape)
            .background(color)
    )
}

/** Nagy, panelen belüli statisztika: érték + cím. */
@Composable
fun FilcMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = filcColors().text,
    contentPadding: PaddingValues = PaddingValues(vertical = 4.dp)
) {
    val filc = filcColors()
    Column(
        modifier = modifier.padding(contentPadding),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = valueColor,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = filc.textMuted,
            maxLines = 1
        )
    }
}
