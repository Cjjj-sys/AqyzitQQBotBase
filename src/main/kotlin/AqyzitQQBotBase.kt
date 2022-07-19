package aqyzit.qqbot.base

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandContext
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.disable
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin.Companion.onLoad
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.info
import java.util.*
import kotlin.coroutines.suspendCoroutine

object AqyzitQQBotBase : KotlinPlugin(
    JvmPluginDescription(
        id = "aqyzit.qqbot.base",
        name = "AqyzitQQBotBase",
        version = "0.1.5",
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

    }

    public var sendPosterTimers: MutableList<SendPosterTimer> = mutableListOf()
    object AboutCommand : RawCommand(
        AqyzitQQBotBase, "about",
        usage = "/about", // 设置用法，将会在 /help 展示
        description = "关于本插件", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            sendMessage("本插件版本0.1.4\nBy:KangKang")
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
                MenuItem("列出所有定时发送海报的任务",ListSendPosterCommand),
                MenuItem("停止定时发送海报的任务",StopSendPosterCommand)
            )
            sendMessage(menu.toMessageChain())
        }
    }

    object SendPosterCommand : RawCommand(
        AqyzitQQBotBase, "sendposter",
        usage = "！需要权限！/sendposter [图片] [间隔(ms, 最小值60000)] [总次数] [群号] [名称]\n" +
                "示例: /sendposter [图片] 60000 10 1234567890 信息社海报1", // 设置用法，将会在 /help 展示
        description = "设置定时发送海报", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            /*var count: Int = 1
            var delayTime: Long = 60000
            val contact = Bot.instances[0].getGroup(args[3].toString().toLong())
            if (args[1].toString().toLong() >= 60000) {
                delayTime = args[1].toString().toLong()
            }
            repeat(args[2].toString().toInt()){
                val chain = buildMessageChain {
                    +args[0]
                    +"\n 总次数${args[2]} 已发送${count} 间隔${delayTime}ms"
                }
                if (contact != null) {
                    contact.sendMessage(chain)
                } else {
                    sendMessage(chain)
                }
                delay(delayTime)
                count += 1*/
            val sendPosterTimer = SendPosterTimer(args[0],
                args[1].toString().toLong(),
                args[2].toString().toInt(),
                args[3].toString().toLong(),
                args[4].toString())
            sendPosterTimers += sendPosterTimer
            sendPosterTimer.run()
            }
        }

    object ListSendPosterCommand : RawCommand(
        AqyzitQQBotBase, "listsendposter",
        usage = "/listsendposter", // 设置用法，将会在 /help 展示
        description = "列出所有定时发送海报的任务", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            var chain = buildMessageChain {
                +PlainText("当前定时发送海报的任务:")
                +PlainText(sendPosterTimers.count().toString() + "\n")
            }
            for (i in 0 until sendPosterTimers.count()) {
                if (sendPosterTimers[i].isAlive){
                    chain += PlainText("${i + 1}: ${sendPosterTimers[i].id} " +
                            "间隔${sendPosterTimers[i].delay}ms " +
                            "已发送${sendPosterTimers[i].count} " +
                            "总次数${sendPosterTimers[i].total} " +
                            "群号${sendPosterTimers[i].groupId}\n")
                } else {
                    sendPosterTimers[i].stopRun()
                    sendPosterTimers.remove(sendPosterTimers[i])
                }
            }
            sendMessage(chain)
        }
    }

    object StopSendPosterCommand : RawCommand(
        AqyzitQQBotBase, "stopsendposter",
        usage = "/stopsendposter [名称]", // 设置用法，将会在 /help 展示
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
}