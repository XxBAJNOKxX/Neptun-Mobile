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

            val classesChannel = NotificationChannel(
                CHANNEL_ID_CLASSES,
                CHANNEL_NAME_CLASSES,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Értesítés az órák megkezdése előtt 15 perccel a terem megjelölésével"
                enableVibration(true)
                setShowBadge(true)
            }

            val messagesChannel = NotificationChannel(
                CHANNEL_ID_MESSAGES,
                CHANNEL_NAME_MESSAGES,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Értesítés az új oktatói és tanulmányi üzenetekről"
                enableVibration(true)
                setShowBadge(true)
            }

            val gradesChannel = NotificationChannel(
                CHANNEL_ID_GRADES,
                CHANNEL_NAME_GRADES,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Értesítés az új érdemjegyekről és félévközi eredményekről"
                enableVibration(true)
                setShowBadge(true)
            }

            val financesChannel = NotificationChannel(
                CHANNEL_ID_FINANCES,
                CHANNEL_NAME_FINANCES,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Értesítés a pénzügyi kiírásokról és határidőkről"
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
            .setContentTitle("Hamarosan kezdődik: $subjectName")
            .setContentText("$courseType $startTime-kor | Terem: $room")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Az órád ($subjectName - $courseType) $minutesBefore perc múlva ($startTime) kezdődik a(z) $room teremben.")
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
            .setContentTitle("Új üzenet: $sender")
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

        val gradeDisplay = when (grade) {
            5 -> "Jeles (5)"
            4 -> "Jó (4)"
            3 -> "Közepes (3)"
            2 -> "Elégséges (2)"
            1 -> "Elégtelen (1)"
            else -> gradeText.ifEmpty { "Új bejegyzés" }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GRADES)
            .setSmallIcon(R.drawable.ic_notif_grade)
            .setContentTitle("Új érdemjegy: $subjectName")
            .setContentText("Eredmény: $gradeDisplay ($credit kredit)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Új értékelés érkezett a(z) $subjectName tantárgyból!\nÉrdemjegy: $gradeDisplay | Kreditérték: $credit")
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
            .setContentTitle("Pénzügyi tétel: $title")
            .setContentText("Összeg: $amount | Határidő: $dueDate")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Figyelem! Befizetendő pénzügyi tétel: $title\nÖsszeg: $amount HUF\nFizetési határidő: $dueDate")
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