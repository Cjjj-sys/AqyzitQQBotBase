package aqyzit.qqbot.base

import kotlinx.serialization.Serializable

@Serializable
class OwnThinkBotResponseRoot(val message: String, val data: OwnThinkBotResponseData) {
}

@Serializable
class OwnThinkBotResponseData(val type: Int, val info: OwnThinkBotResponseDataInfo) {
}

@Serializable
class OwnThinkBotResponseDataInfo(val text: String) {
}