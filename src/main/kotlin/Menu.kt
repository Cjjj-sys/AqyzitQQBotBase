package aqyzit.qqbot.base

import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain

class Menu constructor(vararg menuItems: MenuItem) {
    var menuItems: MutableList<MenuItem> = menuItems.asList().toMutableList()

    fun toMessageChain() : MessageChain{
        var chain = buildMessageChain {
            +PlainText("===== 菜单 =====\n")
        }
        for (i in 0 until menuItems.count()) {
            chain += PlainText("${i+1}: ${menuItems[i].name} ${menuItems[i].command.primaryName}\n")
        }
        chain += PlainText("===== 菜单 =====")
        return chain
    }

    fun addMenuItem(menuItem: MenuItem) {
        this.menuItems += menuItem
    }

    fun removeMenuItemAt(index: Int) {
        this.menuItems.removeAt(index)
    }

    fun removeMenuItem(menuItem: MenuItem) {
        this.menuItems.remove(menuItem)
    }
}