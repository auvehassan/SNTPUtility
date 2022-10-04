package com.auvehassan.ntputility

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var sntpHelper: SNTPHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sntpHelper = SNTPHelper()
        // make sure internet connection is available, otherwise 0 will return
        check()
        findViewById<Button>(R.id.btn).setOnClickListener { check() }
    }

    private fun check(){
        findViewById<TextView>(R.id.textview).text = "Requesting time..."
        if (!isNetworkConnected()){
            findViewById<TextView>(R.id.textview).text = "Check internet connection!"
            return
        }

        Log.d(TAG, "requesting time..")
        val dt = "UTCDate: " + sntpHelper.getUTCDate()
        Log.d(TAG, "dt: $dt")

        GlobalScope.launch {
            val ts = "UTCTimestamp: " + sntpHelper.getUTCTimestamp()
            Log.d(TAG, "ts: $ts")

            val td = async { sntpHelper.getSystemTimeDifference() }
            Log.d(TAG, "td: ${td.await()}")

            withContext(Dispatchers.Main){
                findViewById<TextView>(R.id.textview).text = "$dt \n$ts \nTimeDifference: ${td.await()}"
            }
        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }
}