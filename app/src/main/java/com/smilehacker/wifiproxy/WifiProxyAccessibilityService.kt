package com.smilehacker.wifiproxy

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Handler
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Created by quan.zhou on 2018/5/26.
 */
class WifiProxyAccessibilityService: AccessibilityService() {

    private val WIFI_SETTING_ACTIVITY = "com.android.settings.Settings\$WifiSettingsActivity"
    private val NONE_SSID = "<unknown ssid>"

    private var mInProcess = false

    private val mHandler = Handler()

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
//        if (mInProcess) {
//            return
//        }
        when(event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED  -> handleWindow(event)
        }
    }

    private fun handleWindow(event: AccessibilityEvent) {
        val windowClassName = event.className
        DLog.i("window name = $windowClassName text = ${event.text}")
        if (windowClassName == WIFI_SETTING_ACTIVITY && WifiSettingRepository.enableSetting ) {
            DLog.i("start setting")
            val ssid = readSSID()
            if (ssid == NONE_SSID) {
                return
            }
            performOpenWifiSettingDialog(ssid)
        }
    }

    private fun performOpenWifiSettingDialog(ssid: String) {
        mInProcess = true

        var node = findNodeByText(rootInActiveWindow, ssid) ?: findNodeByText(rootInActiveWindow, "Connected") ?: return
        DLog.i("click current ssid")
        longClickNode(node)

        var modifyNetworkNode = findNodeByText(rootInActiveWindow, "Modify network") ?: return
        DLog.i("click modify window")
        clickNode(modifyNetworkNode)

        mHandler.postDelayed({ performOpenModifyWindow() }, 1000)

    }

    private fun performOpenModifyWindow() {

    }

    private fun readSSID() : String {
        val wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val currentSSID = wifiManager.connectionInfo.ssid
        return currentSSID
    }


    private fun findNodeByText(node: AccessibilityNodeInfo, text: String) : AccessibilityNodeInfo? {
        if (node.childCount == 0) {
            if (node.text?.toString() == text) {
                return node
            }
        } else {
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