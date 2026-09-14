package com.example.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.isBreakOrHolidayName

object NotificationHelper {

    const val CHANNEL_ID_CLASSES = "neptun_classes_channel"
    const val CHANNEL_ID_MESSAGES = "neptun_messages_channel"
    const val CHANNEL_ID_GRADES = "neptun_grades_channel"
    const val CHANNEL_ID_FINANCES = "neptun_finances_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val classesChannel = NotificationChannel(
                CHANNEL_ID_CLASSES,
                context.getString(R.string.notif_channel_classes),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_classes_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            val messagesChannel = NotificationChannel(
                CHANNEL_ID_MESSAGES,
                context.getString(R.string.notif_channel_messages),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_messages_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            val gradesChannel = NotificationChannel(
                CHANNEL_ID_GRADES,
                context.getString(R.string.notif_channel_grades),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_grades_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            val financesChannel = NotificationChannel(
                CHANNEL_ID_FINANCES,
                context.getString(R.string.notif_channel_finances),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_finances_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(classesChannel, messagesChannel, gradesChannel, financesChannel)
            )
        }
    }

    /**
     * Csatornanevek és -leírások frissítése futásidejű nyelvválasztás után.
     * A rendszer a már létező csatornák nevét nem írja felül, ezért törlés +
     * újralétrehozás kell (a felhasználó fontossági beállításai megmaradnak).
     * A [context] legyen az aktuális nyelvre csomagolt kontextus.
     */
    fun refreshNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            listOf(
                CHANNEL_ID_CLASSES,
                CHANNEL_ID_MESSAGES,
                CHANNEL_ID_GRADES,
                CHANNEL_ID_FINANCES
            ).forEach { runCatching { notificationManager.deleteNotificationChannel(it) } }
        }
        createNotificationChannels(context)
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun showClassReminder(
        context: Context,
        notificationId: Int,
        subjectName: String,
        room: String,
        startTime: String,
        courseType: String,
        minutesBefore: Int = 15
    ) {
        // A szerver a bejelentkezési LCID nyelvén küldi a címeket: a szünet-szűrés többnyelvű.
        if (isBreakOrHolidayName(subjectName) || startTime == "00:00" || startTime == "0:00") {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CLASSES)
            .setSmallIcon(R.drawable.ic_notif_class)
            .setContentTitle(context.getString(R.string.notif_class_title, subjectName))
            .setContentText(context.getString(R.string.notif_class_text, courseType, startTime, room))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        context.resources.getQuantityString(
                            R.plurals.notif_class_big,
                            minutesBefore,
                            subjectName,
                            courseType,
                            minutesBefore,
                            startTime,
                            room
                        )
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        safeNotify(context, notificationId, notification)
    }

    fun showMessageNotification(
        context: Context,
        notificationId: Int,
        sender: String,
        subject: String,
        preview: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MESSAGES)
            .setSmallIcon(R.drawable.ic_notif_message)
            .setContentTitle(context.getString(R.string.notif_message_title, sender))
            .setContentText(subject)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$subject\n\n$preview")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        safeNotify(context, notificationId, notification)
    }

    fun showGradeNotification(
        context: Context,
        notificationId: Int,
        subjectName: String,
        grade: Int?,
        gradeText: String,
        credit: Int = 0
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val gradeRes = when (grade) {
            5 -> R.string.grade_5
            4 -> R.string.grade_4
            3 -> R.string.grade_3
            2 -> R.string.grade_2
            1 -> R.string.grade_1
            else -> null
        }
        val gradeDisplay = when {
            gradeRes != null -> context.getString(gradeRes)
            gradeText.isNotEmpty() -> gradeText
            else -> context.getString(R.string.notif_grade_new)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GRADES)
            .setSmallIcon(R.drawable.ic_notif_grade)
            .setContentTitle(context.getString(R.string.notif_grade_title, subjectName))
            .setContentText(
                context.resources.getQuantityString(
                    R.plurals.notif_grade_text,
                    credit,
                    gradeDisplay,
                    credit
                )
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notif_grade_big, subjectName, gradeDisplay, credit))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        safeNotify(context, notificationId, notification)
    }

    /** Több új elem együttes összefoglaló értesítése (pl. "3 új üzenet"). */
    fun showSummaryNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        text: String,
        priorityHigh: Boolean = false
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notif_neptun)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(
                if (priorityHigh) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        safeNotify(context, notificationId, notification)
    }

    fun showFinanceNotification(
        context: Context,
        notificationId: Int,
        title: String,
        amount: String,
        dueDate: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_FINANCES)
            .setSmallIcon(R.drawable.ic_notif_finance)
            .setContentTitle(context.getString(R.string.notif_finance_title, title))
            .setContentText(context.getString(R.string.notif_finance_text, amount, dueDate))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notif_finance_big, title, amount, dueDate))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        safeNotify(context, notificationId, notification)
    }

    /**
     * Értesítés küldése biztonságosan: engedély-ellenőrzéssel és hibakezeléssel,
     * hogy egy hiányzó engedély vagy egy rendszer-specifikus kivétel sose döntse
     * le az appot.
     */
    private fun safeNotify(context: Context, notificationId: Int, notification: android.app.Notification) {
        try {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Értesítés küldése sikertelen: ${e.message}")
        }
    }
}