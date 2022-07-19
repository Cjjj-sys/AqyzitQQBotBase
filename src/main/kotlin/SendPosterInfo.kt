package aqyzit.qqbot.base

import io.ktor.util.reflect.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.findIsInstance
import net.mamoe.mirai.message.data.toMessageChain

@Serializable
class SendPosterInfo(val messageMiraiCode: String, var delay: Long, val total: Int, val groupId: Long,val id: String) {

    fun toJsonString(): String{
        return Json.encodeToString(this)
    }

}