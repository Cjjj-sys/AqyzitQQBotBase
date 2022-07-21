package aqyzit.qqbot.base

import kotlinx.coroutines.CompletableJob
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText

class SendPosterRequest(val requestSender: Contact, val id: String) {

    var chain: MessageChain = MessageChainBuilder().asMessageChain()

    val messageEventListener: CompletableJob =
        GlobalEventChannel.subscribeAlways<MessageEvent> { event ->
            if (event.sender.id == requestSender.id) {
                if (event.message.toString() != "/spr end $id") {
                    chain += event.message
                    chain += PlainText("\n")
                }
            }
        }

    fun end() {
        messageEventListener.cancel()
    }
}