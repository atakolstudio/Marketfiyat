package com.marketfiyat.core.receiver

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.marketfiyat.core.data.local.datastore.UserPreferencesDataStore
import com.marketfiyat.core.domain.repository.BackupRepository
import com.marketfiyat.core.util.Result
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            when (val result = backupRepository.exportToJson()) {
                is com.marketfiyat.core.util.Result.Success -> {
                    saveBackupToFile(result.data)
                    userPreferencesDataStore.setLastBackupTime(System.currentTimeMillis())
                    Timber.d("Backup completed successfully")
                    Result.success()
                }
                is com.marketfiyat.core.util.Result.Error -> {
                    Timber.e(result.exception, "Backup failed")
                    Result.retry()
                }
                else -> Result.failure()
            }
        } catch (e: Exception) {
            Timber.e(e, "Backup worker error")
            Result.failure()
        }
    }

    private fun saveBackupToFile(content: String) {
        val backupDir = File(applicationContext.filesDir, "backup")
        backupDir.mkdirs()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val file = File(backupDir, "backup_$date.json")
        file.writeText(content, Charsets.UTF_8)
        // Keep only last 7 backups
        backupDir.listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?.drop(7)
            ?.forEach { it.delete() }
    }
}
