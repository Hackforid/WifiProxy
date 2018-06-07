package com.smilehacker.automata

import java.util.*

/**
 * Created by quan.zhou on 2018/6/4.
 */
class Action {
    val conditions : MutableList<Condition> = LinkedList()
    var job: Runnable? = null

    var isFinish = false
}

interface Condition

data class EventCondition(val eventType: Int, val className: String): Condition

data class DelayCondition(val delay: Long): Condition
