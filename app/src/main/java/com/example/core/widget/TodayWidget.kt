package com.example.core.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.R
import com.example.data.local.NeptunDatabase
import com.example.domain.model.CalendarEvent
import com.example.domain.usecase.GetTodayClassesUseCase
import kotlinx.coroutines.flow.first

/**
 * Kezdőképernyő-widget: a mai órák listája, az app kártya-arculatához igazítva.
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
        val openAppIntent = Intent(context, MainActivity::class.java)
        provideContent {
            TodayWidgetContent(classes, openAppIntent)
        }
    }

    companion object {
        suspend fun loadTodayClasses(context: Context): List<CalendarEvent> {
            return try {
                val db = NeptunDatabase.getInstance(context)
                val events = db.calendarDao().getAllEvents().first().map { it.toDomain() }
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
private fun TodayWidgetContent(classes: List<CalendarEvent>, openAppIntent: Intent) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(R.color.widget_background))
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(openAppIntent))
            .padding(12.dp)
    ) {
        // Fejléc az app accent színével
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Neptun",
                style = TextStyle(
                    color = ColorProvider(R.color.widget_accent),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                "· Mai órák",
                style = TextStyle(
                    color = ColorProvider(R.color.widget_muted),
                    fontSize = 13.sp
                )
            )
        }

        val actual = classes.filter { it.isActualAttendedClass }
        if (actual.isEmpty()) {
            Spacer(GlanceModifier.height(10.dp))
            Text(
                "Ma nincs több órád!",
                style = TextStyle(
                    color = ColorProvider(R.color.widget_on_background),
                    fontSize = 13.sp
                )
            )
        } else {
            actual.take(3).forEach { event ->
                Spacer(GlanceModifier.height(8.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Idő "chip" – az app kártyáinak stílusa
                    Box(
                        modifier = GlanceModifier
                            .background(ColorProvider(R.color.widget_chip_bg))
                            .cornerRadius(8.dp)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "%02d:%02d".format(event.startHour, event.startMinute),
                            style = TextStyle(
                                color = ColorProvider(R.color.widget_chip_fg),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Spacer(GlanceModifier.width(8.dp))
                    Column {
                        Text(
                            event.subjectName,
                            style = TextStyle(
                                color = ColorProvider(R.color.widget_on_background),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                        Text(
                            listOf(event.courseType.displayName, event.room.takeIf { it.isNotBlank() } ?: "-")
                                .joinToString(" · "),
                            style = TextStyle(
                                color = ColorProvider(R.color.widget_muted),
                                fontSize = 10.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
            if (actual.size > 3) {
                Spacer(GlanceModifier.height(6.dp))
                Text(
                    "…és még ${actual.size - 3} óra",
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_muted),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
