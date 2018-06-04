package com.smilehacker.automata

import java.util.*

/**
 * Created by quan.zhou on 2018/6/4.
 */
class Action {
    val conditions : MutableList<Condition> = LinkedList()
    var job: Runnable? = null
}

data class Condition(val eventType: Int, val className: String) {

}