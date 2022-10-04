package com.auvehassan.ntputility

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.*

class SNTPHelper {

    private var serverGoogle: MutableList<String> = arrayListOf(
        "time.google.com",
        "time1.google.com",
        "time2.google.com",
        "time3.google.com",
        "time4.google.com"
    )

    private var serverFacebook: MutableList<String> = arrayListOf(
        "time.facebook.com",
        "time1.facebook.com",
        "time2.facebook.com",
        "time3.facebook.com",
        "time4.facebook.com"
    )

    private var serverApple: MutableList<String> = arrayListOf(
        "time.apple.com",
        "time1.apple.com",
        "time2.apple.com",
        "time3.apple.com",
        "time4.apple.com",
        "time5.apple.com",
        "time6.apple.com",
        "time7.apple.com",
        "time.euro.apple.com"
    )

    private var serverOther: MutableList<String> = arrayListOf(
        "time.windows.com",
        "pool.ntp.org",
        "europe.pool.ntp.org",
        "asia.pool.ntp.org",
        "oceania.pool.ntp.org",
        "north-america.pool.ntp.org",
        "south-america.pool.ntp.org",
        "africa.pool.ntp.org",
    )

    suspend fun getSystemTimeDifference(): Long {
        var timeZoneDifference: Long = 0
        val sntpClient = SntpClient()

        withContext(Dispatchers.IO) {
            val resultGoogle = async { checkServers(serverGoogle, sntpClient) }
            val resultApple = async { checkServers(serverApple, sntpClient) }
            val resultFacebook = async { checkServers(serverFacebook, sntpClient) }
            val resultOther = async { checkServers(serverOther, sntpClient) }
            timeZoneDifference = resultGoogle.await()

            val listTime = listOf(
                resultGoogle.await(),
                resultApple.await(),
                resultFacebook.await(),
                resultOther.await()
            )

            Log.i(TAG,"getSystemTimeDifference: ${resultGoogle.await()} ${resultApple.await()} ${resultFacebook.await()} ${resultOther.await()}")
            timeZoneDifference = listTime.maxOrNull()!! // set your logic to get time difference
        }

        return timeZoneDifference
    }

    private fun checkServers(servers: MutableList<String>, sntpClient: SntpClient): Long {
        var timeZoneDifference = 0L
        for (i in 0 until servers.size) {
            Log.i(TAG, "checkServers: $i ${servers[i]}")
            try {
                if (sntpClient.requestTime(servers[i], 1000)) {
                    timeZoneDifference = sntpClient.ntpTime
                    timeZoneDifference -= System.currentTimeMillis()
                    if (timeZoneDifference != 0L) return timeZoneDifference
                }
            } catch (e: Exception) {
                Log.e(Companion.TAG, "checkServers ex: ${e.message}")
            }
        }
        return timeZoneDifference
    }

    fun getUTCDate(): Date {
        val nowAsPerDeviceTimeZone = getUTCTimestamp()
        return Date(nowAsPerDeviceTimeZone)
    }

    fun getUTCTimestamp(): Long {
        var nowAsPerDeviceTimeZone: Long = 0
        val sntpClient = SntpClient()
        val success = sntpClient.requestTime("europe.pool.ntp.org", 30000)
        if (success) {
            nowAsPerDeviceTimeZone = sntpClient.ntpTime
            val cal = Calendar.getInstance()
            val timeZoneInDevice = cal.timeZone
            val differentialOfTimeZones = timeZoneInDevice.getOffset(System.currentTimeMillis())
            nowAsPerDeviceTimeZone -= differentialOfTimeZones.toLong()
        }
        return nowAsPerDeviceTimeZone
    }

    companion object {
        private const val TAG = "SNTPHelper"
    }


}