package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.example.core.notification.ClassAlarmReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Ellenőrzi, hogy a kritikus manifest elemek (boot receiver a riasztás-újraütemezéshez,
 * widget receiver, launcher activity) valóban regisztrálva vannak.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ManifestSanityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `boot completed receiver is registered`() {
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED).setPackage(context.packageName)
        val receivers = context.packageManager.queryBroadcastReceivers(intent, PackageManager.GET_META_DATA)
        assertTrue(
            "A BootCompletedReceiver-nek regisztrálva kell lennie a BOOT_COMPLETED intent-re!",
            receivers.any { it.activityInfo.name.contains("BootCompletedReceiver") }
        )
    }

    @Test
    fun `widget receiver is registered`() {
        val intent = Intent("android.appwidget.action.APPWIDGET_UPDATE").setPackage(context.packageName)
        val receivers = context.packageManager.queryBroadcastReceivers(intent, PackageManager.GET_META_DATA)
        assertTrue(
            "A TodayWidgetReceiver-nek regisztrálva kell lennie az APPWIDGET_UPDATE intent-re!",
            receivers.any { it.activityInfo.name.contains("TodayWidgetReceiver") }
        )
    }

    @Test
    fun `main activity is exported launcher`() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        assertNotNull("Launch intent nem található!", intent)
        assertEquals(MainActivity::class.java.name, intent!!.component!!.className)
    }

    @Test
    fun `class alarm receiver is registered`() {
        val intent = Intent(context, ClassAlarmReceiver::class.java)
        val info = context.packageManager.getReceiverInfo(
            intent.component,
            PackageManager.GET_META_DATA
        )
        assertTrue("A ClassAlarmReceiver nem exportálható kifelé!", !info.exported)
    }
}
