package com.smilehacker.wifiproxy

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.smilehacker.automata.AutoMata
import com.smilehacker.automata.DelayCondition
import com.smilehacker.automata.EventCondition
import com.smilehacker.automata.NieR
import com.smilehacker.utils.RxBus
import com.smilehacker.wifiproxy.model.ResetEvent

/**
 * Created by quan.zhou on 2018/5/26.
 */
class WifiProxyAccessibilityService: AccessibilityService() {

    private val TAG = WifiProxyAccessibilityService::class.java.simpleName

    private val WIFI_SETTING_ACTIVITY = "com.android.settings.Settings\$WifiSettingsActivity"
    private val NONE_SSID = "<unknown ssid>"

    private var mInProcess = false
    private var mInProcessOpenModifyNetwork = false
    private var mInProcessOpenProxySetting = false

    private val mHandler = Handler()

    private lateinit var mAutoMate : AutoMata

    override fun onCreate() {
        super.onCreate()
        mAutoMate = NieR.Builder()
            .whenGet(EventCondition(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, WIFI_SETTING_ACTIVITY))
            .then(Runnable {
                openSSIDWifiSettingDialog()
            })
            .whenGet(EventCondition(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, "android.app.AlertDialog"))
            .then(Runnable {
                performOpenModifyWindow()
            })
            .whenGet(EventCondition(AccessibilityEvent.TYPE_VIEW_SELECTED, "android.widget.Spinner"))
            .then(Runnable {
                Log.d(TAG, "dialog open")
                openHigherConfig()
            })
            .whenGet(EventCondition(AccessibilityEvent.TYPE_VIEW_SELECTED, "android.widget.Spinner"))
            .then(Runnable {
                Log.d(TAG, "higher config dialog open")
                openWifiProxy()
            })
            .whenGet(EventCondition(AccessibilityEvent.TYPE_VIEW_CLICKED, "android.widget.Spinner"))
            .then(Runnable {
                selectManualWifiProxySetting()
            })
            .whenGet(DelayCondition(1000))
            .then(Runnable {
                Log.d(TAG, "runrunrun")
                setProxyIP()
            })
            .whenGet(EventCondition(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED, "android.widget.Button"))
            .then(Runnable {
                saveConfig()
            })
            .build()
        init()
    }

    @SuppressLint("CheckResult")
    private fun init() {
        RxBus.toObservableSticky(ResetEvent::class.java)
            .subscribe {
                mAutoMate.stop()
                mAutoMate.start()
            }
    }


    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt")
        mAutoMate.stop()
    }


    override fun onKeyEvent(event: KeyEvent?): Boolean {
        mAutoMate.stop()
        return super.onKeyEvent(event)
    }

    override fun onGesture(gestureId: Int): Boolean {
        mAutoMate.stop()
        return super.onGesture(gestureId)
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        DLog.d("event = " + event.eventType.toString() + " " + event.className + " ${event.contentDescription}")
        mAutoMate.onAccessibilityEvent(event)
    }

    private fun openSSIDWifiSettingDialog() {
        DLog.i("start setting")
        val ssid = readSSID()
        if (ssid == NONE_SSID) {
            return
        }
        DLog.d("find item $ssid")
        var node = findNodeByText(rootInActiveWindow, ssid) ?: return
        DLog.i("click current ssid")
        mInProcessOpenModifyNetwork = true
        longClickNode(node)
    }


    private fun performOpenModifyWindow() {
        var modifyNetworkNode = findNodeByText(rootInActiveWindow, "修改网络") ?: return
        DLog.i("click modify window")
        mInProcessOpenModifyNetwork = false
        mInProcessOpenProxySetting = true
        clickNode(modifyNetworkNode)
    }

    private fun openHigherConfig() {
        val node = findNodeByText(rootInActiveWindow, "高级选项") ?: return
        clickNode(node)
    }

    private fun openWifiProxy() {
        val node = findNodeByText(rootInActiveWindow, "代理") ?: return
        val after = findNodeAfter(node) ?: return
        val settingNode = after.getChild(0) ?: return
        Log.d(TAG, "click proxy setting")
        clickNode(settingNode)
    }

    private fun selectManualWifiProxySetting() {
        val node = findNodeByText(rootInActiveWindow, "手动") ?: return
        Log.d(TAG, "select manual")
        clickNode(node)
    }

    private fun setProxyIP() {
//        findNodeByText(rootInActiveWindow, "代理服务器端口")
        val ipLabelNode = findNodeByText(rootInActiveWindow, "代理服务器主机名") ?: return
        val ipEtNode = findNodeAfter(ipLabelNode) ?: return
        Log.d(TAG, "find text $ipEtNode")
        setText(ipEtNode, "192.168.100.151")

        val portEtNode = findNodeAfter(ipEtNode, 2) ?: return
        setText(portEtNode, "8050")
        Log.d(TAG, "try set ip")
    }

    private fun saveConfig() {
        val button = findNodeByText(rootInActiveWindow, "保存") ?: return
        clickNode(button)
    }

    private fun readSSID() : String {
        val wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val currentSSID = wifiManager.connectionInfo.ssid
        return currentSSID.replace("\"", "")
    }


    private fun findNodeByText(node: AccessibilityNodeInfo, text: String, parent: AccessibilityNodeInfo? = null) : AccessibilityNodeInfo? {
        DLog.d("node text = ${node.text} ${node.className} ${node.hashCode()} parent=${parent?.hashCode()}")
        if (node.text?.toString() == text) {
            return node
        } else if (node.childCount > 0) {
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                val _node = findNodeByText(child, text, node)
                if (_node != null) {
                    return _node
                }
            }

        }
        return null
    }

    private fun findNodeAfter(node: AccessibilityNodeInfo, step : Int = 1) : AccessibilityNodeInfo? {
        val parent = node.parent ?: return null
        for (i in 0 until parent.childCount) {
            val child = parent.getChild(i) ?: continue
            if (child == node && i < parent.childCount - step) {
                return parent.getChild(i + step)
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

    private fun setText(node: AccessibilityNodeInfo, text: String) {
        if (node.className != "android.widget.EditText") {
            return
        }
        val args = Bundle()
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

}