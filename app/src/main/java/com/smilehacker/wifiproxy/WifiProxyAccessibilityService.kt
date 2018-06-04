package com.smilehacker.wifiproxy

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.smilehacker.automata.Condition
import com.smilehacker.automata.NieR

/**
 * Created by quan.zhou on 2018/5/26.
 */
class WifiProxyAccessibilityService: AccessibilityService() {

    private val WIFI_SETTING_ACTIVITY = "com.android.settings.Settings\$WifiSettingsActivity"
    private val NONE_SSID = "<unknown ssid>"

    private var mInProcess = false
    private var mInProcessOpenModifyNetwork = false
    private var mInProcessOpenProxySetting = false

    private val mHandler = Handler()

    override fun onCreate() {
        super.onCreate()
        NieR.Builder()
            .whenGet(Condition(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, WIFI_SETTING_ACTIVITY))
    }

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
//        if (mInProcess) {
//            return
//        }
        DLog.d("event = " + event.eventType.toString() + " " + event.className)
        when(event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED  -> handleWindow(event)
        }

        if (mInProcessOpenProxySetting && event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            performOpenProxySetting()
        }
    }

    private fun handleWindow(event: AccessibilityEvent) {
        val windowClassName = event.className
        if (windowClassName == WIFI_SETTING_ACTIVITY && WifiSettingRepository.enableSetting ) {

            if (!mInProcess) {
                DLog.i("start setting")
                val ssid = readSSID()
                if (ssid == NONE_SSID) {
                    return
                }
                performOpenWifiSettingDialog(ssid)
            }

            if (mInProcessOpenProxySetting) {
                performOpenProxySetting()
            }
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && windowClassName == "android.app.AlertDialog") {
            if (mInProcessOpenModifyNetwork) {
                DLog.i("open modify network")
                performOpenModifyWindow()
            }
        }
    }

    private fun performOpenWifiSettingDialog(ssid: String) {
        mInProcess = true

        DLog.d("find item $ssid")
        var node = findNodeByText(rootInActiveWindow, ssid) ?: return
        DLog.i("click current ssid")
        mInProcessOpenModifyNetwork = true
        longClickNode(node)



//        var modifyNetworkNode = findNodeByText(rootInActiveWindow, "Modify network") ?: return
//        DLog.i("click modify window")
//        clickNode(modifyNetworkNode)
//
//        mHandler.postDelayed({ performOpenModifyWindow() }, 1000)

    }

    private fun performOpenModifyWindow() {
        var modifyNetworkNode = findNodeByText(rootInActiveWindow, "修改网络") ?: return
        DLog.i("click modify window")
        mInProcessOpenModifyNetwork = false
        mInProcessOpenProxySetting = true
        clickNode(modifyNetworkNode)
    }

    private fun performOpenProxySetting() {
        var modifyNetworkNode = findNodeByText(rootInActiveWindow, "修改网络") ?: return
    }

    private fun readSSID() : String {
        val wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val currentSSID = wifiManager.connectionInfo.ssid
        return currentSSID.replace("\"", "")
    }


    private fun findNodeByText(node: AccessibilityNodeInfo, text: String) : AccessibilityNodeInfo? {
        DLog.d("node text = ${node.text}")
        if (node.text?.toString() == text) {
            return node
        } else if (node.childCount > 0) {
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                val _node = findNodeByText(child, text)
                if (_node != null) {
                    return _node
                }
            }

        }
        return null
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        var parent = node
        while(!parent.isClickable) {
            parent = parent.parent ?: break
        }
        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun longClickNode(node: AccessibilityNodeInfo) {
        var parent = node
        while(!parent.isClickable) {
            parent = parent.parent ?: break
        }
        parent.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    }

}