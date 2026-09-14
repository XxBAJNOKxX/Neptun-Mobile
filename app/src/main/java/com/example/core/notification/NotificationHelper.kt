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
import com.example.core.i18n.AppStringsProvider

object NotificationHelper {

    const val CHANNEL_ID_CLASSES = "neptun_classes_channel"
    const val CHANNEL_NAME_CLASSES = "Órarendi Értesítések"

    const val CHANNEL_ID_MESSAGES = "neptun_messages_channel"
    const val CHANNEL_NAME_MESSAGES = "Neptun Üzenetek"

    const val CHANNEL_ID_GRADES = "neptun_grades_channel"
    const val CHANNEL_NAME_GRADES = "Jegyek és Értékelések"

    const val CHANNEL_ID_FINANCES = "neptun_finances_channel"
    const val CHANNEL_NAME_FINANCES = "Pénzügyi Értesítések"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val strings = AppStringsProvider.getForContext(context)

            val classesChannel = NotificationChannel(
                CHANNEL_ID_CLASSES,
                strings.notifChannelClasses,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = strings.notifChannelClassesDesc
                enableVibration(true)
                setShowBadge(true)
            }

            val messagesChannel = NotificationChannel(
                CHANNEL_ID_MESSAGES,
                strings.notifChannelMessages,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = strings.notifChannelMessagesDesc
                enableVibration(true)
                setShowBadge(true)
            }

            val gradesChannel = NotificationChannel(
                CHANNEL_ID_GRADES,
                strings.notifChannelGrades,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = strings.notifChannelGradesDesc
                enableVibration(true)
                setShowBadge(true)
            }

            val financesChannel = NotificationChannel(
                CHANNEL_ID_FINANCES,
                strings.notifChannelFinances,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = strings.notifChannelFinancesDesc
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(
                listOf(classesChannel, messagesChannel, gradesChannel, financesChannel)
            )
        }
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
        val lower = subjectName.lowercase()
        if (lower.contains("szünnap") || lower.contains("szünet") || lower.contains("munkaszünet") ||
            lower.contains("ünnep") || lower.contains("rektori") || lower.contains("dékáni") ||
            startTime == "00:00" || startTime == "0:00"
        ) {
            return
        }

        val strings = AppStringsProvider.getForContext(context)
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
            .setContentTitle(strings.notifClassReminderTitle(subjectName))
            .setContentText(strings.notifClassReminderText(courseType, startTime, room))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(strings.notifClassReminderBigText(subjectName, courseType, minutesBefore, startTime, room))
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
        val strings = AppStringsProvider.getForContext(context)
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
            .setContentTitle(strings.notifNewMessageTitle(sender))
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
        val strings = AppStringsProvider.getForContext(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val gradeDisplay = when (grade) {
            5 -> strings.gradeText5
            4 -> strings.gradeText4
            3 -> strings.gradeText3
            2 -> strings.gradeText2
            1 -> strings.gradeText1
            else -> gradeText.ifEmpty { strings.newEntry }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GRADES)
            .setSmallIcon(R.drawable.ic_notif_grade)
            .setContentTitle(strings.notifNewGradeTitle(subjectName))
            .setContentText(strings.notifNewGradeText(gradeDisplay, credit))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(strings.notifNewGradeBigText(subjectName, gradeDisplay, credit))
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
        val strings = AppStringsProvider.getForContext(context)
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
            .setContentTitle(strings.notifFinanceTitle(title))
            .setContentText(strings.notifFinanceText(amount, dueDate))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(strings.notifFinanceBigText(title, amount, dueDate))
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