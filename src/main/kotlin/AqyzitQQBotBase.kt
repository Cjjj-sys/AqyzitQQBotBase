package aqyzit.qqbot.base

import net.mamoe.mirai.console.command.CommandContext
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.RawCommand
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.info

object AqyzitQQBotBase : KotlinPlugin(
    JvmPluginDescription(
        id = "aqyzit.qqbot.base",
        name = "AqyzitQQBotBase",
        version = "0.1.1",
    ) {
        author("KangKang")
    }
) {
    override fun onEnable() {
        logger.info { "[INFO] 安一信息社QQ机器人基础模块已载入" }
        CommandManager.registerCommand(AboutCommand)
        CommandManager.registerCommand(MainMenuCommand)
    }

    object AboutCommand : RawCommand(
        AqyzitQQBotBase, "about",
        usage = "/about", // 设置用法，将会在 /help 展示
        description = "关于本插件", // 设置描述，将会在 /help 展示
        prefixOptional = true, // 设置指令前缀是可选的，即使用 `test` 也能执行指令而不需要 `/test`
    ) {
        override suspend fun CommandSender.onCommand(args: MessageChain) {
            sendMessage("本插件正在测试")
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
                MenuItem("关于", AboutCommand)
            )
            sendMessage(menu.toMessageChain())
        }
    }
}