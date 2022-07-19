package aqyzit.qqbot.base

import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.buildMessageChain

// val singleMessage: SingleMessage, var delay: Long, val total: Int, val groupId: Long, id: String
class SendPosterTimer constructor(val sendPosterInfo: SendPosterInfo) {
    private var running = false
    var isAlive: Boolean = true
    var count: Int = 1
    val messageMiraiCode = sendPosterInfo.messageMiraiCode
    var delay = sendPosterInfo.delay
    val total = sendPosterInfo.total
    val groupId = sendPosterInfo.groupId
    var id: String = sendPosterInfo.id

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
                val chain = MiraiCode.deserializeMiraiCode(messageMiraiCode)

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