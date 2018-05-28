package com.smilehacker.wifiproxy

import android.content.Context
import android.content.Intent
import android.net.ProxyInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val mWifiManager by lazy { this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }

    private val mTvRua by lazy { findViewById<TextView>(R.id.tv_rua) }
    private val mBtnAccessibility by lazy { findViewById<Button>(R.id.btn_accessibilitiy) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//    RxPermissions(this).request(Manifest.permission.ACCESS_WIFI_STATE)
//        .subscribe { if (it) readWifiConfig() }

        mTvRua.setOnClickListener {
            setWifiByAccessibility()
        }
        mBtnAccessibility.setOnClickListener {
            gotoOpenService()
        }

        WifiSettingRepository.enableSetting = true
    }

    private fun readWifiConfig() {
        val currentNetworkID = mWifiManager.connectionInfo.networkId
        val config = mWifiManager.configuredNetworks.find { it.networkId == currentNetworkID } ?: return

        val proxyIP = "192.168.101.137"
        val proxyPort = 8050



        WifiProxyUtils.setWifiProxySettings(this.applicationContext, proxyIP, proxyPort)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            config.httpProxy = ProxyInfo.buildDirectProxy(proxyIP, proxyPort)
            mWifiManager.updateNetwork(config)
        } else {

        }
    }


    private fun setWifiByAccessibility() {
        gotoWifiSetting()
    }

    private fun gotoWifiSetting() {
        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    private fun gotoOpenService() {
        startActivityForResult(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            123)
    }
}
