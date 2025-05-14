package com.example.lab3

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.app.PendingIntent
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL


class IntentTaskService : IntentService("IntentTaskService") {

    companion object {
        private const val ACTION_DOWNLOAD = "com.example.lab3.action.task1"
        private const val PARAMETER1 = "com.example.lab3.extra.parameter1"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "download_channel"

        const val NOTIFICATION = "com.example.Lab3.MainActivity"

        @JvmStatic
        fun startService(context: Context, parameter: String) {
            val intent = Intent(context, IntentTaskService::class.java).apply {
                action = ACTION_DOWNLOAD
                putExtra(PARAMETER1, parameter)
            }
            context.startService(intent)
        }
    }

    private var mNotificationManager: NotificationManager? = null

    override fun onHandleIntent(intent: Intent?) {
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        prepareDownloadChannel()

        if (intent != null) {
            val action = intent.action
            if (ACTION_DOWNLOAD == action) {
                val param1 = intent.getStringExtra(PARAMETER1) ?: ""
                downloadTask(param1)
            } else {
                Log.d("intent_service", "nieznana akcja")
            }
            Log.d("intent_service", "usługa wykonała zadanie")
        }
    }

    private fun prepareDownloadChannel() {
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var name : CharSequence= getString(R.string.app_name)
            var channel = NotificationChannel(CHANNEL_ID, name,
                NotificationManager.IMPORTANCE_LOW)

            mNotificationManager?.createNotificationChannel(channel)
        }

    }

    private fun createNotification(progress: Int = 0, ongoing: Boolean = true): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setProgress(100, progress, false)
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setWhen(System.currentTimeMillis())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(ongoing)

        return builder.build()
    }


    private fun downloadTask(parameter: String) {
        var fileStream : FileOutputStream? = null
        var inputStream: InputStream? = null
        try {
            val url = URL(parameter)
            var connection = url.openConnection()
            connection.connect()
            inputStream = connection.inputStream

            val fileName = File(url.path).name
            val externalDir = Environment.getExternalStorageDirectory()
            var outputFile = File(externalDir, fileName)
            if(outputFile.exists()){
                outputFile.delete()
            }
            fileStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024)
            var downloaded: Int
            var downloadedBytes = 0L
            var totalSize = connection.contentLength

            mNotificationManager?.notify(NOTIFICATION_ID, createNotification())

            var lastProgress = -1

            while (inputStream.read(buffer).also { downloaded = it } != -1) {
                fileStream.write(buffer, 0, downloaded)
                downloadedBytes += downloaded
                val progress = (downloadedBytes * 100 / totalSize).toInt()
                if (progress != lastProgress) {
                    Log.d("DownloadProgress", "Postęp pobierania: $progress%")
                    val notification = createNotification(progress)
                    mNotificationManager?.notify(NOTIFICATION_ID, notification)
                    sendBroadcast(progress, downloadedBytes)
                    lastProgress = progress
                }
            }
            fileStream.flush()

        }catch(e: Exception){
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            }catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                fileStream?.close()
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendBroadcast(value1 : Int, value2 : Long) {
        val intent = Intent(NOTIFICATION)
        intent.putExtra("progress", value1)
        intent.putExtra("downloadedBytes", value2)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
