package com.example.lab3


import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.net.HttpURLConnection
import java.net.URL
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {

    private lateinit var address : EditText
    private lateinit var sizeView: TextView
    private lateinit var typeView: TextView
    private lateinit var infoButton : Button
    private lateinit var downloadButton : Button
    private lateinit var progressView : TextView
    private lateinit var progressBar : ProgressBar

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val progress = intent.getIntExtra("progress", 0)
            val downloadedBytes = intent.getLongExtra("downloadedBytes", 0L)
            progressBar.progress = progress
            progressView.text = downloadedBytes.toString()
        }
    }

    companion object {
        private const val REQUEST_WRITE_STORAGE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        address = findViewById(R.id.address)
        sizeView = findViewById(R.id.size)
        typeView = findViewById(R.id.type)
        infoButton = findViewById(R.id.info_button)
        downloadButton = findViewById(R.id.file_button)
        progressView = findViewById(R.id.bytes)
        progressBar = findViewById(R.id.progressBar)


        infoButton.setOnClickListener {
            DownloadInfo().execute(address.text.toString())
        }

        downloadButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                IntentTaskService.startService(this, address.text.toString())

            } else {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_WRITE_STORAGE)

            }

        }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(
            IntentTaskService.NOTIFICATION))


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_STORAGE ->
                if (permissions.size > 0 &&
                    permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ){
                    //otrzymaliśmy uprawnienia
                    showToast("Permission granted.")
                    IntentTaskService.startService(this, address.text.toString())
                }else{
                    //nie otrzymaliśmy uprawnień
                    showToast("Permission denied.")
                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private inner class DownloadInfo : AsyncTask<String, Int, Pair<String,String>>() {

        override fun doInBackground(vararg params: String?): Pair<String,String> {

            val address = params[0] ?: return Pair("?","?")
            var connection : HttpURLConnection? = null
            var size = "?"
            var type = "?"

            try {
                var url = URL(address)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                size = connection.contentLength.toString()
                type = connection.contentType ?: "?"
            } catch (e : Exception) {
                e.printStackTrace()
            } finally {
               connection?.disconnect()
            }


            return Pair(size,type)
        }

        override fun onPostExecute(result: Pair<String,String>) {
            sizeView.text = result.first
            typeView.text = result.second
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("address", address.text.toString())
        outState.putString("size", sizeView.text.toString())
        outState.putString("type", typeView.text.toString())
        outState.putString("progress", progressView.text.toString())
        outState.putInt("progressBar", progressBar.progress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        address.setText(savedInstanceState.getString("address", ""))
        sizeView.text = savedInstanceState.getString("size", "?")
        typeView.text = savedInstanceState.getString("type", "?")
        progressView.text = savedInstanceState.getString("progress", "0")
        progressBar.progress = savedInstanceState.getInt("progressBar", 0)
    }
}
