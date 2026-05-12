package com.marketfiyat.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.marketfiyat.core.util.Constants
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                scheduleBackupWorker(context)
            }
        }
    }

    private fun scheduleBackupWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag(Constants.BACKUP_WORKER_TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            Constants.BACKUP_WORKER_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
