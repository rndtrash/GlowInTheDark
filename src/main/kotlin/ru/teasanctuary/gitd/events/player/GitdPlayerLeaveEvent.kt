package ru.teasanctuary.gitd.events.player

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import java.util.*

data class GitdPlayerLeaveEvent(override val player: UUID, override val timestamp: Long) : GitdPlayerEvent(player, timestamp) {
    override fun toChatMessage(): Component {
        val offlinePlayer = Bukkit.getOfflinePlayer(player)
        return MiniMessage.miniMessage().deserialize("Игрок <#55ff55>${offlinePlayer.name}</#55ff55> вышел из сервера.")
    }
}
