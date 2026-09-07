package com.example.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.CalendarEvent
import com.example.domain.model.CourseType
import com.example.ui.theme.filcColors

/**
 * Órarend-sor: bal oldalt az időpont, mellette színes "sín" (a reFilc
 * `LessonTile` leading-jának Compose megfelelője), jobbra tantárgy, terem,
 * oktató és a típus-címke.
 */
@Composable
fun FilcLessonTile(
    event: CalendarEvent,
    modifier: Modifier = Modifier,
    isOngoing: Boolean = false,
    isNext: Boolean = false,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val filc = filcColors()
    val railColor = when {
        isOngoing -> filc.accent
        event.courseType == CourseType.EXAM -> filc.red
        event.courseType == CourseType.LECTURE -> filc.blue
        event.courseType == CourseType.PRACTICE -> filc.green
        event.courseType == CourseType.LAB -> filc.purple
        else -> filc.yellow
    }
    val rowShape = RoundedCornerShape(12.dp)
    val railHeight: Dp = if (compact) 20.dp else 32.dp

    Row(
        modifier = modifier
            .clip(rowShape)
            .then(
                if (isOngoing) Modifier.background(filc.accent.copy(alpha = 0.12f)) else Modifier
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 8.dp, end = 8.dp, top = if (compact) 6.dp else 9.dp, bottom = if (compact) 6.dp else 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(46.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = "%02d:%02d".format(event.startHour, event.startMinute),
                style = MaterialTheme.typography.titleSmall,
                color = if (isOngoing) filc.accent else filc.text,
                maxLines = 1
            )
            if (!compact) {
                Text(
                    text = "%02d:%02d".format(event.endHour, event.endMinute),
                    style = MaterialTheme.typography.labelSmall,
                    color = filc.textMuted,
                    maxLines = 1
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .width(3.dp)
                .height(railHeight)
                .clip(RoundedCornerShape(45.dp))
                .background(railColor)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = event.subjectName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = filc.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(event.room.ifBlank { "–" })
                    if (event.teacherName.isNotBlank()) {
                        append(" · ")
                        append(event.teacherName)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = filc.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        when {
            isOngoing -> FilcChip(
                text = "most",
                color = if (filc.isLight) Color.Black.copy(alpha = 0.75f) else filc.onAccent,
                background = filc.accent
            )

            isNext -> FilcChip(
                text = "köv.",
                color = filc.accent,
                background = filc.accent.copy(alpha = 0.16f)
            )

            else -> CourseTypeBadge(event.courseType)
        }
    }
}

/** Kártyán belüli, halvány cím + érték pár. */
@Composable
fun FilcKeyValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = filcColors().text,
    icon: @Composable (() -> Unit)? = null
) {
    val filc = filcColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(modifier = Modifier.padding(end = 10.dp)) { icon() }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = filc.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Ikonos, lekerekített jelző (szimbólum + halvány háttér). */
@Composable
fun FilcIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = filcColors().accent,
    containerColor: Color = color.copy(alpha = 0.16f),
    size: Dp = 34.dp,
    iconSize: Dp = 17.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(11.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}
