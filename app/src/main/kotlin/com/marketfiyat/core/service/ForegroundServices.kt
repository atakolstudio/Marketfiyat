package com.marketfiyat.core.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.marketfiyat.R
import com.marketfiyat.core.util.NotificationChannels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PriceAlertForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, NotificationChannels.PRICE_ALERT)
            .setContentTitle("Fiyat Takibi Aktif")
            .setContentText("Fiyat alarmları kontrol ediliyor...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        stopSelf()
        return START_NOT_STICKY
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}

@AndroidEntryPoint
class BackupForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, NotificationChannels.BACKUP)
            .setContentTitle("Yedekleme")
            .setContentText("Veriler yedekleniyor...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        stopSelf()
        return START_NOT_STICKY
    }

    companion object {
        private const val NOTIFICATION_ID = 1002
    }
}
