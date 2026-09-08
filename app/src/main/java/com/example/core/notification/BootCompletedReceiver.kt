package com.example.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.core.work.AlarmRescheduleWorker

/**
 * A telefon újraindítása után az összes órarendi riasztás elveszik,
 * ezért itt ütemezzük újra őket a helyi adatbázisból.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) {
            return
        }
        AlarmRescheduleWorker.enqueue(context)
    }
}
