package com.marketfiyat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import com.marketfiyat.core.util.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MarketFiyatApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.ENABLE_LOGGING) android.util.Log.DEBUG
                else android.util.Log.ERROR
            )
            .build()

    override fun onCreate() {
        super.onCreate()
        initTimber()
        initNotificationChannels()
    }

    private fun initTimber() {
        if (BuildConfig.ENABLE_LOGGING) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    private fun initNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val priceAlertChannel = NotificationChannel(
            NotificationChannels.PRICE_ALERT,
            getString(R.string.notification_channel_price_alert),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Fiyat alarmları için bildirim kanalı"
            enableVibration(true)
        }

        val backupChannel = NotificationChannel(
            NotificationChannels.BACKUP,
            getString(R.string.notification_channel_backup),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Yedekleme bildirimleri için kanal"
        }

        notificationManager.createNotificationChannels(
            listOf(priceAlertChannel, backupChannel)
        )
    }

    private inner class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
                // In production, integrate with Firebase Crashlytics or similar
                // FirebaseCrashlytics.getInstance().log(message)
                // t?.let { FirebaseCrashlytics.getInstance().recordException(it) }
            }
        }
    }
}
