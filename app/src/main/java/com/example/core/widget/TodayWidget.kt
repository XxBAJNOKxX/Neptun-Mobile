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
import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity
import com.example.core.security.EncryptedPreferencesManager
import com.example.data.local.NeptunDatabase
import com.example.domain.model.CalendarEvent
import com.example.domain.usecase.GetTodayClassesUseCase
import com.example.ui.theme.AppAccentColor
import com.example.ui.theme.ThemeSettings
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
        val strings = com.example.core.i18n.AppStringsProvider.getForContext(context)
        val themeSettings = try {
            EncryptedPreferencesManager(context).loadThemeSettings()
        } catch (e: Exception) {
            ThemeSettings()
        }
        val accent = themeSettings.accentColor
        provideContent {
            TodayWidgetContent(strings, classes, openAppIntent, accent)
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
private fun TodayWidgetContent(
    strings: com.example.core.i18n.AppStrings,
    classes: List<CalendarEvent>,
    openAppIntent: Intent,
    accent: AppAccentColor = AppAccentColor.BLUE
) {
    val widgetBackground = ColorProvider(
        day = Color(0xFFFFFFFF),
        night = Color(0xFF151E2E)
    )
    val widgetOnBackground = ColorProvider(
        day = Color(0xFF0F172A),
        night = Color(0xFFE2E8F0)
    )
    val widgetAccent = ColorProvider(
        day = accent.lightColorScheme.primary,
        night = accent.darkColorScheme.primary
    )
    val widgetMuted = ColorProvider(
        day = Color(0xFF64748B),
        night = Color(0xFF94A3B8)
    )
    val widgetChipBg = ColorProvider(
        day = accent.lightColorScheme.primaryContainer,
        night = accent.darkColorScheme.primaryContainer
    )
    val widgetChipFg = ColorProvider(
        day = accent.lightColorScheme.onPrimaryContainer,
        night = accent.darkColorScheme.onPrimaryContainer
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(widgetBackground)
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
                    color = widgetAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                "· ${strings.widgetTodayClasses}",
                style = TextStyle(
                    color = widgetMuted,
                    fontSize = 13.sp
                )
            )
        }

        val actual = classes.filter { it.isActualAttendedClass }
        if (actual.isEmpty()) {
            Spacer(GlanceModifier.height(10.dp))
            Text(
                strings.widgetNoMoreClasses,
                style = TextStyle(
                    color = widgetOnBackground,
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
                            .background(widgetChipBg)
                            .cornerRadius(8.dp)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "%02d:%02d".format(event.startHour, event.startMinute),
                            style = TextStyle(
                                color = widgetChipFg,
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
                                color = widgetOnBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                        Text(
                            listOf(event.courseType.getLocalizedName(strings), event.room.takeIf { it.isNotBlank() } ?: "-")
                                .joinToString(" · "),
                            style = TextStyle(
                                color = widgetMuted,
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
                    strings.widgetMoreClassesCount(actual.size - 3),
                    style = TextStyle(
                        color = widgetMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
