package com.example.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.repository.EvidenceRepository
import java.util.concurrent.TimeUnit

class EvidenceUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString(KEY_SESSION_ID) ?: return Result.failure()
        val repository = EvidenceRepository.getInstance(applicationContext)

        val success = repository.performCloudUpload(sessionId)
        return if (success) {
            Result.success()
        } else {
            if (runAttemptCount < 5) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_SESSION_ID = "key_session_id"

        fun enqueueUpload(context: Context, sessionId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val uploadRequest = OneTimeWorkRequestBuilder<EvidenceUploadWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_SESSION_ID to sessionId))
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    15,
                    TimeUnit.SECONDS
                )
                .addTag("evidence_upload_$sessionId")
                .build()

            WorkManager.getInstance(context).enqueue(uploadRequest)
        }
    }
}
