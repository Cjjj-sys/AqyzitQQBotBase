package aqyzit.qqbot.base

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SendPosterUtil {

    fun getSendPosterInfoFromJson(text: String): MutableList<SendPosterInfo> {
        return Json.decodeFromString(text)
    }

}