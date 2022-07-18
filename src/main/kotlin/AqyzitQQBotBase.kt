package aqyzit.qqbot.base

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandContext
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.disable
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
        version = "0.1.4",
    ) {
        author("KangKang")
    }
) {
    override fun onEnable() {
        logger.info { "[INFO] 安一信息社QQ机器人基础模块已载入" }
        CommandManager.registerCommand(AboutCommand)
        CommandManager.registerCommand(MainMenuCommand)
        CommandManager.registerCommand(SendPosterCommand)
        CommandManager.registerCommand(StopSendPosterCommand)

    }

    private var timers: MutableList<Timer> = mutableListOf()
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
                MenuItem("定时发送海报", SendPosterCommand)
            )
            sendMessage(menu.toMessageChain())
        }
    }

    object SendPosterCommand : RawCommand(
        AqyzitQQBotBase, "sendposter",
        usage = "！需要权限！/sendposter [图片] [间隔(ms, 最小值60000)] [总次数] [群号] ", // 设置用法，将会在 /help 展示
        description = "设置定时发送海报", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            var count: Int = 1
            var delayTime: Long = 60000
            var contact = Bot.instances[0].getGroup(args[3].toString().toLong())
            if (args[1].toString().toLong() >= 60000) {
                delayTime = args[1].toString().toLong()
            }
            repeat(args[2].toString().toInt()){
                var chain = buildMessageChain {
                    +args[0]
                    +"\n 总次数${args[2]} 已发送${count} 间隔${delayTime}ms"
                }
                if (contact != null) {
                    contact.sendMessage(chain)
                } else {
                    sendMessage(chain)
                }
                delay(delayTime)
                count += 1
            }
        }
    }

    object StopSendPosterCommand : RawCommand(
        AqyzitQQBotBase, "sendposter",
        usage = "/stopsendposter", // 设置用法，将会在 /help 展示
        description = "取消所有定时发送海报", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            for (timer in timers) {
                timer.cancel()
                timer.purge()
            }
        }
    }
}