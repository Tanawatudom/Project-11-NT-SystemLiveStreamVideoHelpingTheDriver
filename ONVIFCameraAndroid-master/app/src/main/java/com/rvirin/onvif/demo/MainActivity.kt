package com.rvirin.onvif.demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.rvirin.onvif.R
import com.rvirin.onvif.onvifcamera.*

import com.rvirin.onvif.onvifcamera.OnvifRequest.Type.GetStreamURI
import com.rvirin.onvif.onvifcamera.OnvifRequest.Type.GetProfiles
import com.rvirin.onvif.onvifcamera.OnvifRequest.Type.GetDeviceInformation
import com.rvirin.onvif.onvifcamera.OnvifRequest.Type.GetServices
import java.util.ArrayList

const val RTSP_URL = "com.rvirin.onvif.onvifcamera.demo.RTSP_URL"

/**
 * Main activity of this demo project. It allows the user to type his camera IP address,
 * login and password.
 */


class MainActivity : AppCompatActivity(), OnvifListener {

    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val serviceIntent = Intent(this, MyService::class.java)
        startService(serviceIntent)

        val toggle: ToggleButton = findViewById(R.id.togglebtn)
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                stopService(serviceIntent)
            } else {
                startService(serviceIntent)
                // The toggle is disabled
            }
        }


        // create list of show wifi
        //get Wifi local
        //listView = findViewById(R.id.wifiList)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        //check wifi enabled
        if (!wifiManager!!.isWifiEnabled) {
            Toast.makeText(this, "WiFi is disabied ... We need to enble it", Toast.LENGTH_SHORT).show()
            wifiManager!!.isWifiEnabled = true
        }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList)
        com.rvirin.onvif.demo.MainActivity.Companion.listView?.adapter = adapter
        scanWifi(this)
        ip()

    }

    @Suppress("DEPRECATION")
    companion object {
        @JvmField
        var wifiManager: WifiManager? = null
        @JvmField
        var listView: ListView? = null
        @JvmField
        var results: List<ScanResult>? = null
        @JvmField
        var arrayList = ArrayList<String>()
        @JvmField
        var adapter: ArrayAdapter<*>? = null
        var context: Context? = null

        // function scanwifi
        @JvmStatic
        fun scanWifi(context: Context) {
            Log.v("ssv", "in scanWifi")
            arrayList.clear()
            context.registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            wifiManager!!.startScan()
            Log.v("ssv", "can start")
            Toast.makeText(context, "Scaning Wifi ...", Toast.LENGTH_SHORT).show()
        }

        // Broadcast of wifi
        var wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val v = Log.v("ssv", "in broadcast")
                results = wifiManager!!.scanResults
                context.unregisterReceiver(this)
                Log.v("ssv", "can scan")
                for (scanResult in (results as MutableList<ScanResult>?)!!) {
                    arrayList.add(scanResult.SSID)
                    adapter!!.notifyDataSetChanged()
                }
            }
        }

        @JvmStatic
        fun connectWiFi(scanResult: ScanResult) {
            try {
                Log.v("rht", "Item clicked, SSID " + scanResult.SSID + " Security : " + scanResult.capabilities)
                val networkSSID = scanResult.SSID
                val networkPass = "0874929813"
                val conf = WifiConfiguration()
                conf.SSID = "\"" + networkSSID + "\"" // Please note the quotes. String should contain ssid in quotes
                conf.status = WifiConfiguration.Status.ENABLED
                conf.priority = 40
                if (scanResult.capabilities.toUpperCase().contains("WEP")) {
                    Log.v("rht", "Configuring WEP")
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                    conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                    conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                    conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                    conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                    conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                    conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                    //if (networkPass.matches("^[0-9a-fAF]?+$")) {
                    //    conf.wepKeys[0] = networkPass
                    //} else {
                    //     conf.wepKeys[0] = "\"" + networkPass + "\""
                    //}
                    //conf.wepTxKeyIndex = 0
                } else if (scanResult.capabilities.toUpperCase().contains("WPA")) {
                    Log.v("rht", "Configuring WPA")
                    conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                    conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                    conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                    conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                    conf.preSharedKey = "\"" + networkPass + "\""
                } else {
                    Log.v("rht", "Configuring OPEN network")
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                    conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                    conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                    conf.allowedAuthAlgorithms.clear()
                    conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                    conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                }
                val networkId = wifiManager!!.addNetwork(conf)
                Log.v("rht", "Add result $networkId")
                val list = wifiManager!!.configuredNetworks
                for (i in list) {
                    if (i.SSID != null && i.SSID == "\"" + networkSSID + "\"") {
                        Log.v("rht", "WifiConfiguration SSID " + i.SSID)
                        val isDisconnected = wifiManager!!.disconnect()
                        Log.v("rht", "isDisconnected : $isDisconnected")
                        val isEnabled = wifiManager!!.enableNetwork(i.networkId, true)
                        Log.v("rht", "isEnabled : $isEnabled")
                        val isReconnected = wifiManager!!.reconnect()
                        Log.v("rht", "isReconnected : $isReconnected")
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun ip () {
        // If we were able to retrieve information from the camera, and if we have a rtsp uri,
        // We open StreamActivity and pass the rtsp URI
        if (currentDevice.isConnected) {
            currentDevice.rtspURI?.let { uri ->
                val intent = Intent(this, StreamActivity::class.java).apply {
                    putExtra(RTSP_URL, uri)
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "RTSP URI haven't been retrieved", Toast.LENGTH_SHORT).show()
            }
        }else {
            val ipAddress = ("192.168.0.101").toString()
            val login = ("admin").toString()
            val password = ("L2767319").toString()


            // Create ONVIF device with user inputs and retrieve camera informations
            currentDevice = OnvifDevice(ipAddress, login, password)
            currentDevice.listener = this
            currentDevice.getServices()

        }
    }

    override fun requestPerformed(response: OnvifResponse) {

        Log.d("INFO", response.parsingUIMessage)

        toast?.cancel()

        if (!response.success) {
            Log.e("ERROR", "request failed: ${response.request.type} \n Response: ${response.error}")
            toast = Toast.makeText(this, "⛔️ Request failed: ${response.request.type}", Toast.LENGTH_SHORT)
            toast?.show()
        }
        // if GetServices have been completed, we request the device information
            else if (response.request.type == GetServices) {
            currentDevice.getDeviceInformation()
        }
        // if GetDeviceInformation have been completed, we request the profiles
        else if (response.request.type == GetDeviceInformation) {

            val textView = findViewById<TextView>(R.id.explanationTextView)
            textView.text = response.parsingUIMessage
            toast = Toast.makeText(this, "Device information retrieved 👍", Toast.LENGTH_SHORT)
            toast?.show()

            currentDevice.getProfiles()

        }
        // if GetProfiles have been completed, we request the Stream URI
        else if (response.request.type == GetProfiles) {
            val profilesCount = currentDevice.mediaProfiles.count()
            toast = Toast.makeText(this, "$profilesCount profiles retrieved 😎", Toast.LENGTH_SHORT)
            toast?.show()

            currentDevice.getStreamURI()

        }
        // if GetStreamURI have been completed, we're ready to play the video
        else if (response.request.type == GetStreamURI) {

            val button = findViewById<TextView>(R.id.button)
            button.text = getString(R.string.Play)

            toast = Toast.makeText(this, "Stream URI retrieved,\nready for the movie 🍿", Toast.LENGTH_SHORT)
            toast?.show()
        }
    }


}
