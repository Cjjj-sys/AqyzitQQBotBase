package aqyzit.qqbot.base

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Bot.Companion.instances
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.info
import java.io.File
import kotlin.collections.HashMap

object AqyzitQQBotBase : KotlinPlugin(
    JvmPluginDescription(
        id = "aqyzit.qqbot.base",
        name = "AqyzitQQBotBase",
        version = "0.1.7",
    ) {
        author("KangKang")
    }
) {
    override fun onEnable() {
        logger.info { "[INFO] 安一信息社QQ机器人基础模块已载入" }
        CommandManager.registerCommand(AboutCommand)
        CommandManager.registerCommand(MainMenuCommand)
        CommandManager.registerCommand(SendPosterCommand)
        CommandManager.registerCommand(ListSendPosterCommand)
        CommandManager.registerCommand(StopSendPosterCommand)
        CommandManager.registerCommand(SaveSendPosterCommand)
        CommandManager.registerCommand(SendPosterRequestCommand)
        CommandManager.registerCommand(ListSendPosterRequestCommand)
        CommandManager.registerCommand(AcceptSendPosterRequestCommand)
        CommandManager.registerCommand(RefuseSendPosterRequestCommand)

        val botInvitedJoinGroupRequestEventListener: CompletableJob =
            GlobalEventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> { event ->
                event.accept()
            }

        val newFriendRequestEventListener: CompletableJob =
            GlobalEventChannel.subscribeAlways<NewFriendRequestEvent> { event ->
                event.accept()
            }

        val botOnlineEventListener: CompletableJob =
            GlobalEventChannel.subscribeAlways<BotOnlineEvent> { event ->
                if (File("save.json").exists()) {
                    val jsonText = File("save.json").readText()
                    val sendPosterInfoList = SendPosterUtil().getSendPosterInfoFromJson(jsonText)
                    for (sendPosterInfo in sendPosterInfoList) {
                        delay(60000)
                        logger.info { "[INFO] 载入任务 ${sendPosterInfo.id}" }
                        launch {
                            val sendPosterTimer = SendPosterTimer(sendPosterInfo)
                            sendPosterTimers += sendPosterTimer
                            sendPosterTimer.run()
                        }
                    }
                }
            }

        val groupMessageEventListener: CompletableJob =
            GlobalEventChannel.subscribeAlways<GroupMessageEvent> { event ->
                if (event.message[1].toString() == "[mirai:at:2971105266]") {
                    launch { val msg = event.message[2].toString().trim()
                        val client = HttpClient.newBuilder().build();
                        val request = HttpRequest.newBuilder()
                            .uri(URI.create("http://api.qingyunke.com/api.php?key=free&appid=0&msg=${msg}"))
                            .build();

                        val response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        val resultChatData = Json.decodeFromString<ChatData>(response.body())
                        event.group.sendMessage(At(sender.id) + resultChatData.content
                            .replace("{br}", "\n")
                            .replace("小美人菲菲", "小帅哥轩轩")
                            .replace("菲菲", "轩轩")
                            .replace("<BR>", " ")
                            .replace("&nbsp", " ")
                            .replace("纯情小女生","纯情小男生")
                        )
                    }
                }
            }
    }

    public var sendPosterTimers: MutableList<SendPosterTimer> = mutableListOf()
    object AboutCommand : RawCommand(
        AqyzitQQBotBase, "about",
        usage = "/about", // 设置用法，将会在 /help 展示
        description = "关于本插件", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            sendMessage("!!测试中!!\n本插件版本${version}\nBy:KangKang\n欢迎加入信息社!")
        }
    }

    object MainMenuCommand : RawCommand(
        AqyzitQQBotBase, "menu",
        usage = "/menu", // 设置用法，将会在 /help 展示
        description = "显示主菜单", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            val menu = Menu(
                MenuItem("主菜单", MainMenuCommand),
                MenuItem("关于", AboutCommand),
                MenuItem("定时发送海报", SendPosterCommand),
                MenuItem("列出所有定时发送海报的任务", ListSendPosterCommand),
                MenuItem("停止定时发送海报的任务", StopSendPosterCommand),
                MenuItem("将当前海报任务保存到本地", SaveSendPosterCommand),
                MenuItem("发送定时发送海报请求", SendPosterRequestCommand),
                MenuItem("列出所有定时发送海报的请求", ListSendPosterRequestCommand),
                MenuItem("接受定时发送海报请求", AcceptSendPosterRequestCommand),
                MenuItem("拒绝定时发送海报请求", RefuseSendPosterRequestCommand)
            )
            sendMessage(menu.toMessageChain())
        }
    }

    object SendPosterCommand : RawCommand(
        AqyzitQQBotBase, "sendposter","sp",
        usage = "!!需要权限!!/sendposter [图片] [间隔(分钟, 最小值1)] [总次数] [群号] [名称]\n" +
                "示例: /sendposter [图片] 1 10 1234567890 信息社海报1", // 设置用法，将会在 /help 展示
        description = "设置定时发送海报", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            val sendPosterInfo = SendPosterInfo(args[0].toMessageChain().serializeToMiraiCode(),
                args[1].toString().toLong()*60000,
                args[2].toString().toInt(),
                args[3].toString().toLong(),
                args[4].toString())
            val sendPosterTimer = SendPosterTimer(sendPosterInfo)
            sendPosterTimers += sendPosterTimer
            sendPosterTimer.run()
            }
        }

    var preSendPosterRequestHashMap: HashMap<String, SendPosterRequest> = HashMap()
    var sendPosterRequestList: MutableList<SendPosterRequest> = mutableListOf()
    object SendPosterRequestCommand : RawCommand(
        AqyzitQQBotBase, "sendposterrequest","spr",
        usage = "/spr start [名称]\n[多行内容]\n/spr end [名称]\n" +
                "示例: /spr start 信息社海报\n[图片]\n在2022同学群发10次,每次间隔半小时\n/spr end 信息社海报", // 设置用法，将会在 /help 展示
        description = "发送定时发送海报请求", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            if (args[0].toString() == "start") {
                val sendPosterRequest = SendPosterRequest(this.subject!!, args[1].toString())
                preSendPosterRequestHashMap[args[1].toString()] = sendPosterRequest
            }
            else if (args[0].toString() == "end") {
                if (preSendPosterRequestHashMap[args[1].toString()] != null) {
                    val sendPosterRequest = preSendPosterRequestHashMap[args[1].toString()]
                    sendPosterRequest!!.end()
                    preSendPosterRequestHashMap.remove(args[1].toString())
                    sendPosterRequestList += sendPosterRequest
                }
            }
        }
    }

    object ListSendPosterRequestCommand : RawCommand(
        AqyzitQQBotBase, "listsendposterrequest","lspr",
        usage = "/lspr", // 设置用法，将会在 /help 展示
        description = "列出所有定时发送海报的请求", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            var chain = buildMessageChain {
                +PlainText("当前定时发送海报的请求:")
                +PlainText(sendPosterRequestList.count().toString() + "\n")
            }
            for (i in 0 until sendPosterRequestList.count()) {
                chain += "${sendPosterRequestList[i].id}(发送者: ${sendPosterRequestList[i].requestSender}): "
                chain += sendPosterRequestList[i].chain
                chain += "\n"
            }
            sendMessage(chain)
        }
    }

    object ListSendPosterCommand : RawCommand(
        AqyzitQQBotBase, "listsendposter","lsp",
        usage = "!!需要权限!!/listsendposter", // 设置用法，将会在 /help 展示
        description = "列出所有定时发送海报的任务", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            var chain = buildMessageChain {
                +PlainText("当前定时发送海报的请求:")
                +PlainText(sendPosterTimers.count().toString() + "\n")
            }
            val removeList = mutableListOf<SendPosterTimer>()
            for (i in 0 until sendPosterTimers.count()) {
                if (sendPosterTimers[i].isAlive){
                    chain += PlainText("${i + 1}: ${sendPosterTimers[i].id} " +
                            "间隔${sendPosterTimers[i].delay/60000}分钟 " +
                            "已发送${sendPosterTimers[i].count} " +
                            "总次数${sendPosterTimers[i].total} " +
                            "群号${sendPosterTimers[i].groupId}\n")
                } else {
                    sendPosterTimers[i].stopRun()
                    removeList += sendPosterTimers[i]
                }
            }
            for (sendPosterTimerToRemove in removeList) {
                sendPosterTimers.remove(sendPosterTimerToRemove)
            }
            sendMessage(chain)
        }
    }

    object StopSendPosterCommand : RawCommand(
        AqyzitQQBotBase, "stopsendposter","stsp",
        usage = "!!需要权限!!/stopsendposter [名称]", // 设置用法，将会在 /help 展示
        description = "停止定时发送海报", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            for (i in 0 until sendPosterTimers.count()) {
                if (sendPosterTimers[i].id == args[0].toString()) {
                    sendPosterTimers[i].stopRun()
                    sendMessage("已停止 ${sendPosterTimers[i].id}")
                    sendPosterTimers.remove(sendPosterTimers[i])
                }
            }
        }
    }

    object RefuseSendPosterRequestCommand : RawCommand(
        AqyzitQQBotBase, "refusesendposterrequest","rspr",
        usage = "!!需要权限!!/rspr [名称] [原因]", // 设置用法，将会在 /help 展示
        description = "拒绝定时发送海报请求,并给出原因", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            for (i in 0 until sendPosterRequestList.count()) {
                if (sendPosterRequestList[i].id == args[0].toString()) {
                    sendMessage("已拒绝 ${sendPosterRequestList[i].id}, 原因是 ${args[1]}")
                    sendPosterRequestList[i].requestSender.sendMessage("已拒绝 ${sendPosterRequestList[i].id}, 原因是 ${args[1]}")
                    sendPosterRequestList.remove(sendPosterRequestList[i])
                }
            }
        }
    }

    object AcceptSendPosterRequestCommand : RawCommand(
        AqyzitQQBotBase, "acceptsendposterrequest","aspr",
        usage = "!!需要权限!!/aspr ", // 设置用法，将会在 /help 展示
        description = "接受定时发送海报请求", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            for (i in 0 until sendPosterRequestList.count()) {
                if (sendPosterRequestList[i].id == args[0].toString()) {
                    sendMessage("已接受 ${sendPosterRequestList[i].id}")
                    sendPosterRequestList[i].requestSender.sendMessage("已接受 ${sendPosterRequestList[i].id}")
                    sendPosterRequestList.remove(sendPosterRequestList[i])
                }
            }
        }
    }

    object SaveSendPosterCommand : RawCommand(
        AqyzitQQBotBase, "savesendposter","sasp",
        usage = "!!需要权限!!/savesendposter", // 设置用法，将会在 /help 展示
        description = "将当前海报任务保存到本地", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            val sendPosterInfoList: MutableList<SendPosterInfo> = mutableListOf()
            for (i in 0 until sendPosterTimers.count()) {
                val sendPosterTimer = sendPosterTimers[i]
                val sendPosterInfo = sendPosterTimer.sendPosterInfo
                sendPosterInfoList += sendPosterInfo
                sendMessage("已保存 ${sendPosterInfo.id}")
            }
            val sendPosterInfoListJson = Json.encodeToString(sendPosterInfoList)
            File("save.json").writeText(sendPosterInfoListJson)
        }
    }
}
