package com.smilehacker.automata

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

/**
 * Created by quan.zhou on 2018/6/7.
 */
class AutoMata(private val mActions: List<Action>) {

    private var mCurrentAction: Action? = null
    private var mCurrentActionPos: Int = -1

    private var mStart = false

    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    fun start() {
        mCurrentActionPos = -1
        mStart = true
        next()
    }

    fun stop() {
        mStart = false
        mActions.forEach { it.isFinish = false }
    }

    fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!mStart) {
            return
        }
        mCurrentAction?.conditions?.forEach {
            when(it) {
                is EventCondition -> {
                    if (it.eventType == event.eventType && it.className == event.className) {
                        handleAction(mCurrentAction!!)
                        return
                    }
                }
            }
        }
    }

    private fun handleAction(action: Action) {
        if (action.isFinish) {
            return
        }
        action.job?.run()
        action.isFinish = true
        next()
        checkDelayCondition()
    }

    private fun checkDelayCondition() {
        val action = mCurrentAction ?: return
        action.conditions.forEach {
            if (it is DelayCondition) {
                mHandler.postDelayed({
                    if (action == mCurrentAction && !action.isFinish) {
                        handleAction(action)
                    }
                }, it.delay)
            }
        }
    }

    private fun next() {
        if (mCurrentActionPos + 1  >= mActions.size) {
            mCurrentAction = null
            return
        }
        mCurrentActionPos++
        mCurrentAction = mActions[mCurrentActionPos]
    }
}
