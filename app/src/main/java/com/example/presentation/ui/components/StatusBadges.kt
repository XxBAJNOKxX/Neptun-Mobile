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
    val (bgColor, textColor) = when (courseType) {
        CourseType.LECTURE -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
        CourseType.PRACTICE -> Color(0xFFF0FDF4) to Color(0xFF15803D)
        CourseType.LAB -> Color(0xFFFAF5FF) to Color(0xFF7E22CE)
        CourseType.SEMINAR -> Color(0xFFFFFBEB) to Color(0xFFB45309)
        CourseType.EXAM -> Color(0xFFFEF2F2) to Color(0xFFB91C1C)
    }

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
    val (bgColor, textColor) = if (isGhost) {
        Color(0xFFEDE9FE) to NeptunPurple
    } else {
        when (grade) {
            5 -> Color(0xFFDCFCE7) to NeptunGreen
            4 -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
            3 -> Color(0xFFFEF3C7) to NeptunGold
            2 -> Color(0xFFFFEDD5) to Color(0xFFC2410C)
            1 -> Color(0xFFFEE2E2) to NeptunRed
            else -> Color(0xFFF1F5F9) to Color(0xFF64748B)
        }
    }

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
    val (bgColor, textColor) = when (status) {
        FinanceStatus.COMPLETED -> Color(0xFFDCFCE7) to NeptunGreen
        FinanceStatus.PENDING -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        FinanceStatus.OVERDUE -> Color(0xFFFEE2E2) to NeptunRed
    }

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
