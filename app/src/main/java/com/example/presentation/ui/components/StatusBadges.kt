package com.example.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CourseType
import com.example.domain.model.FinanceStatus
import com.example.ui.theme.NeptunGold
import com.example.ui.theme.NeptunGreen
import com.example.ui.theme.NeptunPurple
import com.example.ui.theme.NeptunRed

@Composable
fun CourseTypeBadge(courseType: CourseType, modifier: Modifier = Modifier) {
    val textColor = when (courseType) {
        CourseType.LECTURE -> Color(0xFF3B82F6)
        CourseType.PRACTICE -> NeptunGreen
        CourseType.LAB -> NeptunPurple
        CourseType.SEMINAR -> NeptunGold
        CourseType.EXAM -> NeptunRed
    }
    val bgColor = textColor.copy(alpha = 0.15f)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = courseType.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun GradeBadge(grade: Int?, gradeText: String, isGhost: Boolean = false, modifier: Modifier = Modifier) {
    val textColor = if (isGhost) {
        NeptunPurple
    } else {
        when (grade) {
            5 -> NeptunGreen
            4 -> Color(0xFF0284C7)
            3 -> NeptunGold
            2 -> Color(0xFFEA580C)
            1 -> NeptunRed
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    val bgColor = textColor.copy(alpha = 0.15f)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            if (isGhost) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NeptunPurple)
                )
                Text(
                    text = " Szellem: ",
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = if (grade != null) "$grade" else gradeText,
                color = textColor,
                fontSize = if (grade != null) 14.sp else 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun FinanceStatusBadge(status: FinanceStatus, modifier: Modifier = Modifier) {
    val textColor = when (status) {
        FinanceStatus.COMPLETED -> NeptunGreen
        FinanceStatus.PENDING -> Color(0xFFD97706)
        FinanceStatus.OVERDUE -> NeptunRed
    }
    val bgColor = textColor.copy(alpha = 0.15f)

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
