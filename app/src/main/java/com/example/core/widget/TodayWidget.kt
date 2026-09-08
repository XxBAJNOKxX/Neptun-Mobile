package com.example.core.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.local.NeptunDatabase
import com.example.domain.model.CalendarEvent
import com.example.domain.usecase.GetTodayClassesUseCase
import kotlinx.coroutines.flow.first

/**
 * Kezdőképernyő-widget: a mai órák listája, koppintásra megnyitja az appot.
 */
class TodayWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 110.dp),
            DpSize(250.dp, 110.dp),
            DpSize(250.dp, 180.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val classes = loadTodayClasses(context)
        provideContent {
            TodayWidgetContent(classes)
        }
    }

    companion object {
        suspend fun loadTodayClasses(context: Context): List<CalendarEvent> {
            return try {
                val db = NeptunDatabase.getInstance(context)
                val events = db.calendarDao().getAllEvents().first()
                GetTodayClassesUseCase()(events)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}

@Composable
private fun TodayWidgetContent(classes: List<CalendarEvent>) {
    val backgroundColor = ColorProvider(day = Color(0xFFF1F5F9), night = Color(0xFF0F172A))
    val textColor = ColorProvider(day = Color(0xFF0F172A), night = Color(0xFFE2E8F0))
    val accentColor = ColorProvider(day = Color(0xFF1E40AF), night = Color(0xFF93C5FD))
    val mutedColor = ColorProvider(day = Color(0xFF64748B), night = Color(0xFF94A3B8))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
    ) {
        Text(
            "Neptun · Ma",
            style = TextStyle(color = accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        )
        Spacer(GlanceModifier.height(6.dp))

        val actual = classes.filter { it.isActualAttendedClass }
        if (actual.isEmpty()) {
            Text(
                "Ma nincs több órád 🎉",
                style = TextStyle(color = textColor, fontSize = 13.sp)
            )
        } else {
            actual.take(3).forEach { event ->
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    "${event.timeFormatted}  ${event.subjectName}",
                    style = TextStyle(color = textColor, fontSize = 12.sp)
                )
                Text(
                    listOf(event.courseType.displayName, event.room.takeIf { it.isNotBlank() } ?: "-")
                        .joinToString(" · "),
                    style = TextStyle(color = mutedColor, fontSize = 11.sp)
                )
            }
            if (actual.size > 3) {
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    "…és még ${actual.size - 3} óra",
                    style = TextStyle(color = mutedColor, fontSize = 11.sp)
                )
            }
        }
    }
}
