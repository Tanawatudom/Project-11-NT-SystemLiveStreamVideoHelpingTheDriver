package com.rvirin.onvif.demo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.widget.Toast
import com.rvirin.onvif.R
import com.rvirin.onvif.demo.MainActivity.Companion.connectWiFi
import java.util.concurrent.TimeUnit

open class MyService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Service running", Toast.LENGTH_SHORT).show()
        val info = MainActivity.wifiManager!!.connectionInfo

        val ssid = info.ssid
        Thread(Runnable {
            startBroadcast()
            while (true) {
                val info = MainActivity.wifiManager!!.connectionInfo
                val ssid = info.ssid
                try {
                    Log.v("ssv", "1 : ssid is $ssid")
                    if (ssid != "\"MOOMOO!!V2\"") {
                        Log.v("ssv", "2 : ssid is not wifi target ")
                        checkWifiTarget()
                        Thread.sleep(10000)
                    } else {
                        Log.v("ssv", "0 :ssid is wifi target")
                        delay()
                        Thread.sleep(10000)
                    }
                } catch (e: InterruptedException) {
                    Log.v("ssv", "test loop 10 sec")
                    e.printStackTrace()
                }
            }
        }).start()
        return START_STICKY
    }

    fun startNotification() {
        createNotificationChannel()
        val notificationIntent = Intent(this, StreamActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)
        val notification = NotificationCompat.Builder(this, App.CHANNEL_ID)
                .setContentTitle("Service running")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_baseline)
                .setContentIntent(pendingIntent)
                .build()
        Log.v("ssv", "set notification")

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, notification)
        }
    }

    fun checkWifiTarget() {
        MainActivity.wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        MainActivity.arrayList.clear()
        registerReceiver(wifiReceiver1, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        MainActivity.wifiManager!!.startScan()
    }

    fun startBroadcast() {
        Log.v("ssv", "come start_broadcast()")
        MainActivity.wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        MainActivity.arrayList.clear()
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        MainActivity.wifiManager!!.startScan()
        Log.v("ssv", "out start_broadcast()")
    }

    fun delay() {
        try {
            TimeUnit.SECONDS.sleep(20)
            Log.v("ssv", "delay 1 min")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    var wifiReceiver1: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            MainActivity.results = MainActivity.wifiManager!!.scanResults
            unregisterReceiver(this)
            Log.v("ssv", "scan wifi of check wifi target")
            for (scanResult in (MainActivity.results as MutableList<ScanResult>?)!!) {
                MainActivity.arrayList.add(scanResult.SSID)
                MainActivity.adapter!!.notifyDataSetChanged()
                Log.v("ssv", "!! fond Wifi !!")
                if (scanResult.SSID == "MOOMOO!!V2") {
                    Log.v("ssv", "********* target ************")
                    connectWiFi(scanResult)
                    Log.v("ssv", "********* connected ***********")
                    Log.v("ssv", "!! start notification !!")
                    startNotification()
                    delay()
                }
            }
        }
    }
    var wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            MainActivity.results = MainActivity.wifiManager!!.scanResults
            unregisterReceiver(this)
            Log.v("ssv", "scan wifi of start broadcast")
            for (scanResult in (MainActivity.results as MutableList<ScanResult>?)!!) {
                MainActivity.arrayList.add(scanResult.SSID)
                MainActivity.adapter!!.notifyDataSetChanged()
                Log.v("ssv", "!!fond!!")
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ServiceChannel"
            val descriptionText = "ServiceChannel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("ServiceChannel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}