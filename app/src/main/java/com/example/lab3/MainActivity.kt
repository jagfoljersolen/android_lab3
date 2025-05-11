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
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var address : EditText
    private lateinit var sizeView: TextView
    private lateinit var typeView: TextView
    private lateinit var infoButton : Button
    private lateinit var downloadButton : Button

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


        infoButton.setOnClickListener {
            DownloadInfo().execute(address.text.toString())
        }

        downloadButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                IntentTaskService.startService(this, address.text.toString())

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_WRITE_STORAGE)
                }
            }

        }


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

        override fun onProgressUpdate(vararg values: Int?) {
            // aktualizacja informacji o postępie
        }

        override fun onPostExecute(result: Pair<String,String>) {
            sizeView.text = result.first
            typeView.text = result.second
        }
    }

}
