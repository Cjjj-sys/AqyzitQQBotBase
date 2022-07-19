package aqyzit.qqbot.base

import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.buildMessageChain

class SendPosterTimer(val singleMessage: SingleMessage, var delay: Long, val total: Int, val groupId: Long, id: String) {
    private var running = false
    public var isAlive: Boolean = true
    var count: Int = 1

    var id: String = id

    suspend fun run() {
        var delayTime: Long = 60000
        var contact = Bot.instances[0].getGroup(groupId)
        if (delay >= 60000) {
            delayTime = delay
            delay = delayTime
        }
        running = true
        repeat(total) {
            if (running){
                val chain = buildMessageChain {
                    +singleMessage
                    //+"\n 总次数${total} 已发送${count} 间隔${delayTime}ms"
                }
                if (contact != null) {
                    contact!!.sendMessage(chain)
                } else {
                    contact = Bot.instances[0].getGroup(921072287)
                    contact?.sendMessage(chain)
                }
                delay(delayTime)
                count += 1
                if (count == total){
                    isAlive = false
                }
            }
        }
    }

    fun stopRun() {
        running = false
    }

    fun continueRun() {
        running = true
    }
}