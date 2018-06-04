package com.smilehacker.automata

import java.util.*

/**
 * Created by quan.zhou on 2018/6/4.
 */
class NieR {

    class Builder {
        private var actions = LinkedList<Action>()


        fun whenGet(condition: Condition) : Builder {
            val action = Action()
            action.conditions.add(condition)
            actions.add(action)
            return this
        }

        fun then(runnable: Runnable) : Builder {
            val action = actions.last
            action.job = runnable
            return this
        }

        fun build() : AutoMata {
            val autoMata = AutoMata()
            autoMata.actions = actions
            return AutoMata()
        }

    }

    class AutoMata {
        var actions = LinkedList<Action>()
    }

}